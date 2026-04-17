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
package uq.pac.childclinic.doctor;

import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.Value;

import jakarta.servlet.http.HttpSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import uq.pac.childclinic.cedar.CedarAuthorization;
import uq.pac.childclinic.cedar.CedarRequest;
import uq.pac.childclinic.cedar.CedarService;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class DoctorController {

	private final DoctorRepository doctorRepository;

	private final CedarService cedarService;

	public DoctorController(DoctorRepository doctorRepository, CedarService cedarService) {
		this.doctorRepository = doctorRepository;
		this.cedarService = cedarService;
	}

	@GetMapping("/doctors.html")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String showDoctorsList(@RequestParam(defaultValue = "1") int page, Model model, HttpSession session) {
		// Here we are returning an object of type 'Doctors' rather than a collection of Doctor
		// objects so it is simpler for Object-Xml mapping
		Doctors doctors = new Doctors();

		String principalId = (String) session.getAttribute("currentUser");
		if (session.getAttribute("currentUser") == null) {
			principalId = "Guest";
		}

		System.out.println("Cookie principalId: " + principalId);

		EntityUID principal;
		if (principalId.equals("Guest")) {
			principal = EntityUID.parse("ChildClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			principal = EntityUID.parse("ChildClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}

		Collection<Doctor> allDoctors = doctorRepository.findAll();

		List<Doctor> authorizedDoctors = allDoctors.stream().filter(doctor -> {
			EntityUID action = EntityUID.parse("ChildClinic::Action::\"" + "ViewEmployee" + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
			EntityUID resource = EntityUID
				.parse("ChildClinic::Employee::\"" + doctor.getFirstName() + " " + doctor.getLastName() + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));
			Map<String, Value> contextMap = new HashMap<>();
			CedarRequest cedarReq = new CedarRequest(principal, action, resource, contextMap, true);
			ResponseEntity<String> response = cedarService.checkAccess(cedarReq);
			return response.getBody().startsWith("Access Granted.");
		}).collect(Collectors.toList());

		doctors.getDoctorsList().addAll(authorizedDoctors);

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorizedDoctors.size());
		
		List<Doctor> pageContent;
		if (start > authorizedDoctors.size()) {
		    pageContent = List.of();
		} else {
		    pageContent = authorizedDoctors.subList(start, end);
		}

		Page<Doctor> filteredPaginated = new PageImpl<>(pageContent, pageable, authorizedDoctors.size());

		return addPaginationModel(page, filteredPaginated, model);
	}

	private String addPaginationModel(int page, Page<Doctor> paginated, Model model) {
		List<Doctor> listDoctors = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listDoctors", listDoctors);
		return "doctors/doctorsList";
	}

	@GetMapping({ "/doctors" })
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public @ResponseBody Doctors showResourcesDoctorsList(HttpSession session) {
		// Here we are returning an object of type 'Doctors' rather than a collection of Doctor
		// objects so it is simpler for JSon/Object mapping
		Doctors doctors = new Doctors();

		String principalId = (String) session.getAttribute("currentUser");
		if (principalId == null) {
			principalId = "Guest";
		}

		EntityUID principal;
		if (principalId.equals("Guest")) {
			principal = EntityUID.parse("ChildClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			principal = EntityUID.parse("ChildClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}

		List<Doctor> authorizedDoctors = this.doctorRepository.findAll().stream().filter(doctor -> {
			EntityUID action = EntityUID.parse("ChildClinic::Action::\"" + "ViewEmployee" + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
			EntityUID resource = EntityUID
				.parse("ChildClinic::Employee::\"" + doctor.getFirstName() + " " + doctor.getLastName() + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));
			Map<String, Value> contextMap = new HashMap<>();
			CedarRequest cedarReq = new CedarRequest(principal, action, resource, contextMap, true);
			ResponseEntity<String> response = cedarService.checkAccess(cedarReq);
			return response.getBody().startsWith("Access Granted.");
		}).collect(Collectors.toList());

		doctors.getDoctorsList().addAll(authorizedDoctors);
		return doctors;
	}

}
