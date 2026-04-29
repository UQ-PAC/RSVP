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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import uq.pac.childclinic.cedar.CedarAuthorization;
import uq.pac.childclinic.cedar.CedarRequest;
import uq.pac.childclinic.cedar.CedarService;
import uq.pac.childclinic.system.Clinic;
import uq.pac.childclinic.system.ClinicRepository;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class DoctorController {

	private static final String VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM = "doctors/createOrUpdateDoctorForm";

	private final DoctorRepository doctors;

	private final SpecialtyRepository specialties;

	private final ClinicRepository clinics;

	private final CedarService cedarService;

	public DoctorController(DoctorRepository doctors, SpecialtyRepository specialties, ClinicRepository clinics,
			CedarService cedarService) {
		this.doctors = doctors;
		this.specialties = specialties;
		this.clinics = clinics;
		this.cedarService = cedarService;
	}

	@ModelAttribute("specialties")
	public Collection<Specialty> populateSpecialties() {
		return this.specialties.findAll();
	}

	@ModelAttribute("clinics")
	public Collection<Clinic> populateClinics() {
		return this.clinics.findClinics();
	}

	@InitBinder("doctor")
	public void initDoctorBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/doctors/find")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initFindForm(Model model) {
		model.addAttribute("doctor", new Doctor());
		return "doctors/findDoctors";
	}

	@GetMapping("/doctors")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String showDoctorsList(@RequestParam(defaultValue = "1") int page, Doctor doctor, BindingResult result,
			Model model, HttpSession session) {
		String lastName = doctor.getLastName() == null ? "" : doctor.getLastName();

		List<Doctor> allMatchingDoctors = this.doctors.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingDoctors.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No doctors found.");
			return "doctors/findDoctors";
		}

		String principalId = (String) session.getAttribute("currentUser");
		principalId = principalId == null ? "Guest" : principalId;

		System.out.println("Cookie principalId: " + principalId);

		EntityUID principal;
		if (principalId.equals("Guest")) {
			principal = EntityUID.parse("ChildrenClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			principal = EntityUID.parse("ChildrenClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}

		EntityUID action = EntityUID.parse("ChildrenClinic::Action::\"ViewEmployee\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));

		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Doctor> authorized = allMatchingDoctors.stream().filter(d -> {
			String resourceName = d.getFirstName() + " " + d.getLastName();
			EntityUID resource = EntityUID.parse("ChildrenClinic::Employee::\"" + resourceName + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

			String access = cedarService
				.checkAccess(new CedarRequest(principal, action, resource, new HashMap<>(), true))
				.getBody();
			authorizationMap.put(d.getId(), access);
			cedarResourceMap.put(d.getId(), "ChildrenClinic::Employee::\"" + resourceName + "\"");
			return access != null && access.startsWith("Access Granted.");
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingDoctors.size() == 1) {
			return "redirect:/doctors/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Doctor> pageContent = start > authorized.size() ? List.of()
				: authorized.subList(start, end);
		Page<Doctor> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listDoctors", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", action.toString());
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		return "doctors/doctorsList";
	}

	@GetMapping("/doctors/{doctorId}")
	@CedarAuthorization(action = "ViewEmployee", resourceType = "Employee", validate = true)
	public ModelAndView showDoctor(@PathVariable("doctorId") int doctorId) {
		ModelAndView mav = new ModelAndView("doctors/doctorDetails");
		Doctor doctor = this.doctors.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found for identifier: " + doctorId));
		mav.addObject("doctor", doctor);
		return mav;
	}

	@GetMapping("/doctors/new")
	@CedarAuthorization(action = "AddEmployee", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initCreationForm(Model model) {
		model.addAttribute("doctor", new Doctor());
		return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/doctors/new")
	@CedarAuthorization(action = "AddEmployee", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processCreationForm(@Valid Doctor doctor, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.hasLength(doctor.getLastName()) && StringUtils.hasLength(doctor.getFirstName())
				&& doctor.isNew()) {
			boolean duplicateExists = doctors.findByLastNameStartingWith(doctor.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(d -> d.getFirstName().equalsIgnoreCase(doctor.getFirstName()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "A doctor with this first and last name already exists.");
			}
		}
		if (result.hasErrors())
			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		this.doctors.save(doctor);
		redirectAttributes.addFlashAttribute("message", "New Doctor has been added.");
		return "redirect:/doctors/" + doctor.getId();
	}

	@GetMapping("/doctors/{doctorId}/edit")
	@CedarAuthorization(action = "EditEmployee", resourceType = "Employee", validate = true)
	public String initUpdateForm(@PathVariable("doctorId") int doctorId, Model model) {
		Doctor doctor = this.doctors.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found for identifier: " + doctorId));
		model.addAttribute("doctor", doctor);
		return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/doctors/{doctorId}/edit")
	@CedarAuthorization(action = "EditEmployee", resourceType = "Employee", validate = true)
	public String processUpdateForm(@Valid Doctor doctor, BindingResult result, @PathVariable("doctorId") int doctorId,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.hasLength(doctor.getLastName()) && StringUtils.hasLength(doctor.getFirstName())) {
			boolean duplicateExists = this.doctors
				.findByLastNameStartingWith(doctor.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(d -> d.getFirstName().equalsIgnoreCase(doctor.getFirstName())
						&& !Objects.equals(d.getId(), doctorId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "A doctor with this first and last name already exists.");
			}
		}
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the doctor.");
			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		doctor.setId(doctorId);
		this.doctors.save(doctor);

		redirectAttributes.addFlashAttribute("message", "Doctor Values Updated.");
		return "redirect:/doctors/{doctorId}";
	}

}
