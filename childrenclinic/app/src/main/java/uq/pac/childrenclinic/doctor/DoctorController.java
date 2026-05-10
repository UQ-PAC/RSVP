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
package uq.pac.childrenclinic.doctor;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
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

import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarDeniedException;
import uq.pac.childrenclinic.cedar.CedarEntitiesInvalidationEvent;
import uq.pac.childrenclinic.cedar.CedarProgrammaticEvaluator;
import uq.pac.childrenclinic.model.Gender;
import uq.pac.childrenclinic.model.GenderRepository;
import uq.pac.childrenclinic.system.Clinic;
import uq.pac.childrenclinic.system.ClinicRepository;

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

	private final GenderRepository genders;

	private final SpecialtyRepository specialties;

	private final ClinicRepository clinics;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	private final ApplicationEventPublisher eventPublisher;

	public DoctorController(DoctorRepository doctors, GenderRepository genders, SpecialtyRepository specialties,
			ClinicRepository clinics, CedarProgrammaticEvaluator cedarEvaluator,
			ApplicationEventPublisher eventPublisher) {
		this.doctors = doctors;
		this.genders = genders;
		this.specialties = specialties;
		this.clinics = clinics;
		this.cedarEvaluator = cedarEvaluator;
		this.eventPublisher = eventPublisher;
	}

	@ModelAttribute("genders")
	public Collection<Gender> populateGenders() {
		return this.genders.findGenders();
	}

	@ModelAttribute("specialties")
	public Collection<Specialty> populateSpecialties() {
		return this.specialties.findAll();
	}

	@ModelAttribute("clinics")
	public Collection<Clinic> populateClinics(HttpSession session) {
		Collection<Clinic> allClinics = this.clinics.findClinics();
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		if (allClinics == null)
			return new ArrayList<>();

		return allClinics.stream().filter(clinic -> {
			if (clinic == null || clinic.getClinicName() == null)
				return false;
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var result = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Item");
			return result.isGranted();
		}).collect(Collectors.toList());
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

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Doctor> authorized = allMatchingDoctors.stream().filter(d -> {
			String resourceName = d.getFirstName() + " " + d.getLastName();
			var evalResult = cedarEvaluator.evaluate(principal, "ViewEmployee", "Employee", resourceName, "Item");
			authorizationMap.put(d.getId(), evalResult.responseBody());
			cedarResourceMap.put(d.getId(), "ChildrenClinic::Employee::\"" + resourceName + "\"");
			return evalResult.isGranted();
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingDoctors.size() == 1) {
			return "redirect:/doctors/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Doctor> pageContent = start > authorized.size() ? List.of() : authorized.subList(start, end);
		Page<Doctor> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listDoctors", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", "ChildrenClinic::Action::\"ViewEmployee\"");
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
	public String initCreationForm(Model model, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Collection<Clinic> allClinics = this.clinics.findClinics();

		// Evaluate Cedar for each Clinic and log the result.
		boolean isAuthorized = false;
		List<String> denialReasons = new ArrayList<>();

		if (allClinics != null) {
			for (Clinic clinic : allClinics) {
				if (clinic == null || clinic.getClinicName() == null)
					continue;
				String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var result = cedarEvaluator.evaluate(principal, "AddEmployee", "Clinic", cedarClinicId, "Page");

				if (result.isGranted()) {
					isAuthorized = true;
				}
				else if (result.responseBody() != null) {
					denialReasons.add(result.responseBody());
				}
			}
		}

		// Deny Access if no clinics passed the check.
		if (!isAuthorized) {
			String prefix = "Access Denied.\n";
			StringBuilder exceptionBody = new StringBuilder("Access Denied by the Cedar Policy Engine.\n\n");

			if (!denialReasons.isEmpty()) {
				// Combine all denial reasons and strip out the redundant prefix from
				// each.
				for (String reason : denialReasons) {
					exceptionBody.append(reason.replaceAll("(?m)^" + prefix, "")).append("\n");
				}
			}
			else {
				exceptionBody.append("You do not have permission to add doctors to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		model.addAttribute("doctor", new Doctor());
		return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/doctors/new")
	public String processCreationForm(@Valid Doctor doctor, BindingResult result, RedirectAttributes redirectAttributes,
			HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		// Evaluate Cedar for all the submitted Clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		Collection<Clinic> submittedClinics = doctor.getClinics();

		if (submittedClinics == null || submittedClinics.isEmpty()) {
			isAuthorized = false;
			denialReasons.add("You must assign the Doctor to at least one valid Clinic.");
		}
		else {
			for (Clinic clinic : submittedClinics) {
				if (clinic == null || clinic.getClinicName() == null)
					continue;
				String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var evalResult = cedarEvaluator.evaluate(principal, "AddEmployee", "Clinic", cedarClinicId, "Page");

				if (!evalResult.isGranted()) {
					isAuthorized = false;
					if (evalResult.responseBody() != null) {
						denialReasons.add(evalResult.responseBody());
					}
				}
			}
		}

		// Deny Access if any checks failed.
		if (!isAuthorized) {
			String prefix = "Access Denied.\n";
			StringBuilder exceptionBody = new StringBuilder("Access Denied by the Cedar Policy Engine.\n\n");

			if (!denialReasons.isEmpty()) {
				for (String reason : denialReasons) {
					exceptionBody.append(reason.replaceAll("(?m)^" + prefix, "")).append("\n");
				}
			}
			else {
				exceptionBody
					.append("You do not have permission to add doctors to one or more of the selected clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		if (StringUtils.hasLength(doctor.getLastName()) && StringUtils.hasLength(doctor.getFirstName())
				&& doctor.isNew()) {
			boolean duplicateExists = doctors.findByLastNameStartingWith(doctor.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(d -> d.getFirstName().equalsIgnoreCase(doctor.getFirstName())
						&& Objects.equals(d.getBirthDate(), doctor.getBirthDate())
						&& Objects.equals(d.getGender(), doctor.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A doctor with this first and last name, birth date, and gender already exists.");
			}
		}

		if (result.hasErrors()) {
			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		try {
			this.doctors.save(doctor);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
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
	public String processUpdateForm(@Valid Doctor doctor, BindingResult result, @PathVariable("doctorId") int doctorId,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Doctor existingDoctor = this.doctors.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

		String resourceName = existingDoctor.getFirstName() + " " + existingDoctor.getLastName();
		var doctorEval = cedarEvaluator.evaluate(principal, "EditEmployee", "Employee", resourceName, "Page");

		if (!doctorEval.isGranted()) {
			throw new CedarDeniedException("Access Denied: You do not have permission to edit this doctor.\n"
					+ (doctorEval.responseBody() != null ? doctorEval.responseBody() : ""));
		}

		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();
		Collection<Clinic> submittedClinics = doctor.getClinics();

		if (submittedClinics != null) {
			for (Clinic clinic : submittedClinics) {
				if (clinic == null || clinic.getClinicName() == null)
					continue;
				String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				// Here we check for the "AddEmployee" action, instead of "EditEmployee",
				// since the former applies to the "Clinic" resource.
				var clinicEval = cedarEvaluator.evaluate(principal, "AddEmployee", "Clinic", cedarClinicId, "Page");
				if (!clinicEval.isGranted()) {
					isAuthorized = false;
					if (clinicEval.responseBody() != null) {
						denialReasons.add(clinicEval.responseBody());
					}
				}
			}
		}

		if (!isAuthorized) {
			String prefix = "Access Denied.\n";
			StringBuilder exceptionBody = new StringBuilder("Access Denied by the Cedar Policy Engine.\n\n");
			for (String reason : denialReasons) {
				exceptionBody.append(reason.replaceAll("(?m)^" + prefix, "")).append("\n");
			}
			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		Set<Clinic> finalClinics = new HashSet<>(submittedClinics != null ? submittedClinics : new ArrayList<>());

		if (existingDoctor.getClinics() != null) {
			for (Clinic existingClinic : existingDoctor.getClinics()) {
				if (existingClinic == null || existingClinic.getClinicName() == null)
					continue;
				String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Background");

				if (!viewEval.isGranted()) {
					finalClinics.add(existingClinic);
				}
			}
		}
		doctor.setClinics(finalClinics);

		if (StringUtils.hasLength(doctor.getLastName()) && StringUtils.hasLength(doctor.getFirstName())) {
			boolean duplicateExists = this.doctors
				.findByLastNameStartingWith(doctor.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(d -> d.getFirstName().equalsIgnoreCase(doctor.getFirstName())
						&& Objects.equals(d.getBirthDate(), doctor.getBirthDate())
						&& Objects.equals(d.getGender(), doctor.getGender()) && !Objects.equals(d.getId(), doctorId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A doctor with this first and last name, birth date, and gender already exists.");
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("error", "There was an error in updating the doctor.");
			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		doctor.setId(doctorId);

		try {
			this.doctors.save(doctor);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("error", "There was an error in updating the doctor.");
			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "Doctor values updated.");
		return "redirect:/doctors/{doctorId}";
	}

}
