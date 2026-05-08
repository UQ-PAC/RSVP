/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uq.pac.childrenclinic.system;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarProgrammaticEvaluator;
import uq.pac.childrenclinic.doctor.Doctor;
import uq.pac.childrenclinic.doctor.DoctorRepository;

@Controller
class WelcomeController {

	private final DoctorRepository doctors;

	private final ClinicRepository clinics;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	public WelcomeController(DoctorRepository doctors, ClinicRepository clinics,
			CedarProgrammaticEvaluator cedarEvaluator) {
		this.doctors = doctors;
		this.clinics = clinics;
		this.cedarEvaluator = cedarEvaluator;
	}

	@GetMapping("/")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String welcome(Model model, HttpSession session) {
		List<Doctor> allDoctors = this.doctors.findByLastNameStartingWith("", Pageable.unpaged()).getContent();
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Doctor> authorized = allDoctors.stream().filter(d -> {
			String resourceName = d.getFirstName() + " " + d.getLastName();
			var evalResult = cedarEvaluator.evaluate(principal, "ViewEmployee", "Employee", resourceName, "Item");
			authorizationMap.put(d.getId(), evalResult.responseBody());
			cedarResourceMap.put(d.getId(), "ChildrenClinic::Employee::\"" + resourceName + "\"");
			return evalResult.isGranted();
		}).collect(Collectors.toList());

		model.addAttribute("listDoctors", authorized);
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", "ChildrenClinic::Action::\"ViewEmployee\"");
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		Map<String, String> searchOptions = new LinkedHashMap<>();
		Map<String, String> addOptions = new LinkedHashMap<>();

		if (isAuthorized(principal, "ListPatients")) {
			searchOptions.put("patient", "Patient");
		}
		if (isAuthorized(principal, "ListAdults")) {
			searchOptions.put("adult", "Adult");
		}
		if (isAuthorized(principal, "ListEmployees")) {
			searchOptions.put("secretary", "Secretary");
			searchOptions.put("doctor", "Doctor");
		}

		if (isAuthorized(principal, "AddPatient")) {
			addOptions.put("/patients/new", "Patient");
		}
		if (isAuthorized(principal, "AddAdult")) {
			addOptions.put("/adults/new", "Adult");
		}
		if (isAuthorized(principal, "AddEmployee")) {
			addOptions.put("/secretaries/new", "Secretary");
			addOptions.put("/doctors/new", "Doctor");
		}

		model.addAttribute("searchOptions", searchOptions);
		model.addAttribute("hasSearchAccess", !searchOptions.isEmpty());
		model.addAttribute("addOptions", addOptions);

		return "welcome";
	}

	@GetMapping("/search")
	public String processGlobalSearch(@RequestParam(name = "entityType", defaultValue = "patient") String entityType,
			@RequestParam(name = "query", defaultValue = "") String query, HttpSession session) {

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		String encodedQuery = UriUtils.encode(query, StandardCharsets.UTF_8);

		if ("patient".equalsIgnoreCase(entityType) && isAuthorized(principal, "ListPatients")) {
			return "redirect:/patients?lastName=" + encodedQuery;
		}
		else if ("adult".equalsIgnoreCase(entityType) && isAuthorized(principal, "ListAdults")) {
			return "redirect:/adults?lastName=" + encodedQuery;
		}
		else if ("secretary".equalsIgnoreCase(entityType) && isAuthorized(principal, "ListEmployees")) {
			return "redirect:/secretaries?lastName=" + encodedQuery;
		}
		else if ("doctor".equalsIgnoreCase(entityType) && isAuthorized(principal, "ListEmployees")) {
			return "redirect:/doctors?lastName=" + encodedQuery;
		}
		else {
			return "redirect:/";
		}
	}

	private boolean isAuthorized(EntityUID principal, String actionStr) {
		Collection<Clinic> allClinics = this.clinics.findClinics();
		boolean hasAccess = false;

		for (Clinic clinic : allClinics) {
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");

			var result = cedarEvaluator.evaluate(principal, actionStr, "Clinic", cedarClinicId, "Item");

			// If access is granted for AT LEAST ONE clinic, the user gets the menu option
			if (result.isGranted()) {
				hasAccess = true;
			}
		}

		return hasAccess;
	}

}
