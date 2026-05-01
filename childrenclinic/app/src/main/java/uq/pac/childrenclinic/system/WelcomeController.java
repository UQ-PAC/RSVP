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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarLogContext;
import uq.pac.childrenclinic.cedar.CedarRequest;
import uq.pac.childrenclinic.cedar.CedarService;
import uq.pac.childrenclinic.doctor.Doctor;
import uq.pac.childrenclinic.doctor.DoctorRepository;

@Controller
class WelcomeController {

	private final DoctorRepository doctors;

	private final ClinicRepository clinics;

	private final CedarService cedarService;

	private final CedarLogContext cedarLogContext;

	public WelcomeController(DoctorRepository doctors, ClinicRepository clinics, CedarService cedarService, CedarLogContext cedarLogContext) {
		this.doctors = doctors;
		this.clinics = clinics;
		this.cedarService = cedarService;
		this.cedarLogContext = cedarLogContext;
	}

	@GetMapping("/")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String welcome(Model model, HttpSession session) {
		List<Doctor> allDoctors = this.doctors.findByLastNameStartingWith("", Pageable.unpaged()).getContent();

		EntityUID principal = resolvePrincipal(session);

		EntityUID action = EntityUID.parse("ChildrenClinic::Action::\"ViewEmployee\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));

		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		for (Doctor d : allDoctors) {
			String resourceName = d.getFirstName() + " " + d.getLastName();
			EntityUID resource = EntityUID.parse("ChildrenClinic::Employee::\"" + resourceName + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

			String access = cedarService
				.checkAccess(new CedarRequest(principal, action, resource, new HashMap<>(), true))
				.getBody();
			authorizationMap.put(d.getId(), access);
			cedarResourceMap.put(d.getId(), "ChildrenClinic::Employee::\"" + resourceName + "\"");
		}

		model.addAttribute("listDoctors", allDoctors);
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", action.toString());
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

		EntityUID principal = resolvePrincipal(session);

		if ("patient".equalsIgnoreCase(entityType) && isAuthorized(principal, "ListPatients")) {
			return "redirect:/patients?lastName=" + query;
		}
		else if ("adult".equalsIgnoreCase(entityType) && isAuthorized(principal, "ListAdults")) {
			return "redirect:/adults?lastName=" + query;
		}
		else if ("secretary".equalsIgnoreCase(entityType) && isAuthorized(principal, "ListEmployees")) {
			return "redirect:/secretaries?lastName=" + query;
		}
		else if ("doctor".equalsIgnoreCase(entityType) && isAuthorized(principal, "ListEmployees")) {
			return "redirect:/doctors?lastName=" + query;
		}
		else {
			return "redirect:/";
		}
	}

	private EntityUID resolvePrincipal(HttpSession session) {
		String principalId = (String) session.getAttribute("currentUser");
		principalId = principalId == null ? "Guest" : principalId;

		if (principalId.equals("Guest")) {
			return EntityUID.parse("ChildrenClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			return EntityUID.parse("ChildrenClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
	}

	private boolean isAuthorized(EntityUID principal, String actionStr) {
		EntityUID action = EntityUID.parse("ChildrenClinic::Action::\"" + actionStr + "\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));

		Collection<Clinic> allClinics = this.clinics.findClinics();
		boolean hasAccess = false;

		for (Clinic clinic : allClinics) {
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");

			EntityUID resource = EntityUID.parse("ChildrenClinic::Clinic::\"" + cedarClinicId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

			CedarRequest request = new CedarRequest(principal, action, resource, new HashMap<>(), true);
			String accessBody = cedarService.checkAccess(request).getBody();

			String logEntry = "Item Request: Principal=" + principal + ", Action=" + action + ", Resource=" + resource
					+ " | Response: " + accessBody;
			this.cedarLogContext.addLog(logEntry);

			// If access is granted for AT LEAST ONE clinic, the user gets the menu option
			if (accessBody != null && accessBody.startsWith("Access Granted.")) {
				hasAccess = true;
			}
		}

		return hasAccess;
	}

}
