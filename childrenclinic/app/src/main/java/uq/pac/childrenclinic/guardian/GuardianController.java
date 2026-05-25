/*
 * Copyright 2012-2025 the original author or authors.
 * Copyright 2026 Gabriel Henrique Lopes Gomes Alves Nunes.
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
 *
 * This file has been modified from the original Spring PetClinic project
 * (https://github.com/spring-projects/spring-petclinic).
 */

package uq.pac.childrenclinic.guardian;

import java.nio.charset.StandardCharsets;
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
import org.springframework.web.util.UriUtils;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarDeniedException;
import uq.pac.childrenclinic.cedar.CedarEntitiesInvalidationEvent;
import uq.pac.childrenclinic.cedar.CedarProgrammaticEvaluator;
import uq.pac.childrenclinic.model.Gender;
import uq.pac.childrenclinic.model.GenderRepository;
import uq.pac.childrenclinic.patient.GuardianAuthority;
import uq.pac.childrenclinic.patient.GuardianAuthorityRepository;
import uq.pac.childrenclinic.patient.Patient;
import uq.pac.childrenclinic.patient.PatientFormState;
import uq.pac.childrenclinic.patient.PatientGuardian;
import uq.pac.childrenclinic.patient.PatientRepository;
import uq.pac.childrenclinic.system.Clinic;
import uq.pac.childrenclinic.system.ClinicRepository;

@Controller
public class GuardianController {

	private static final String VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM = "guardians/createOrUpdateGuardianForm";

	private final GuardianRepository guardians;

	private final GenderRepository genders;

	private final ClinicRepository clinics;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	private final ApplicationEventPublisher eventPublisher;

	private final PatientRepository patients;

	private final GuardianAuthorityRepository authorities;

	public GuardianController(GuardianRepository guardians, GenderRepository genders, ClinicRepository clinics,
			CedarProgrammaticEvaluator cedarEvaluator, ApplicationEventPublisher eventPublisher,
			PatientRepository patients, GuardianAuthorityRepository authorities) {
		this.guardians = guardians;
		this.genders = genders;
		this.clinics = clinics;
		this.cedarEvaluator = cedarEvaluator;
		this.eventPublisher = eventPublisher;
		this.patients = patients;
		this.authorities = authorities;
	}

	@ModelAttribute("genders")
	public Collection<Gender> populateGenders() {
		return this.genders.findGenders();
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

	@InitBinder("guardian")
	public void initGuardianBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/guardians")
	@CedarAuthorization(action = "ListGuardians", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Guardian guardian, BindingResult result,
			Model model, HttpSession session) {
		String lastName = guardian.getLastName() == null ? "" : guardian.getLastName();

		List<Guardian> allMatchingGuardians = this.guardians.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingGuardians.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No guardians found.");
			return "redirect:/?error=noGuardians&query=" + UriUtils.encode(lastName, StandardCharsets.UTF_8);
		}

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Guardian> authorized = allMatchingGuardians.stream().filter(a -> {
			String resourceName = a.getFirstName() + " " + a.getLastName();
			var evalResult = cedarEvaluator.evaluate(principal, "ViewGuardian", "Guardian", resourceName, "Item");
			authorizationMap.put(a.getId(), evalResult.responseBody());
			cedarResourceMap.put(a.getId(), "ChildrenClinic::Guardian::\"" + resourceName + "\"");
			return evalResult.isGranted();
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingGuardians.size() == 1) {
			return "redirect:/guardians/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Guardian> pageContent = start > authorized.size() ? List.of() : authorized.subList(start, end);
		Page<Guardian> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listGuardians", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", "ChildrenClinic::Action::\"ViewGuardian\"");
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		return "guardians/guardiansList";
	}

	/**
	 * Retrieves and renders the details of a specific Guardian entity.
	 */
	@GetMapping("/guardians/{guardianId}")
	@CedarAuthorization(action = "ViewGuardian", resourceType = "Guardian", validate = true)
	public ModelAndView showGuardian(@PathVariable("guardianId") int guardianId, HttpSession session) {
		ModelAndView mav = new ModelAndView("guardians/guardianDetails");
		Guardian guardian = this.guardians.findById(guardianId)
			.orElseThrow(() -> new IllegalArgumentException("Guardian not found with identifier: " + guardianId));
		mav.addObject("guardian", guardian);

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		String resourceName = guardian.getFirstName() + " " + guardian.getLastName();
		var editEval = cedarEvaluator.evaluate(principal, "EditGuardian", "Guardian", resourceName, "Background");
		mav.addObject("canEdit", editEval.isGranted());

		return mav;
	}

	@GetMapping("/guardians/new")
	public String initCreationForm(@RequestParam(name = "patientId", required = false) Integer patientId,
			@RequestParam(name = "fromPatientForm", required = false) Boolean fromPatientForm, Model model,
			HttpSession session) {
		if (!Boolean.TRUE.equals(fromPatientForm) && patientId == null) {
			session.removeAttribute("patientFormState");
		}

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Collection<Clinic> allClinics = this.clinics.findClinics();

		boolean isAuthorized = false;
		List<String> denialReasons = new ArrayList<>();

		if (allClinics != null) {
			for (Clinic clinic : allClinics) {
				if (clinic == null || clinic.getClinicName() == null)
					continue;
				String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var result = cedarEvaluator.evaluate(principal, "AddGuardian", "Clinic", cedarClinicId, "Page");

				if (result.isGranted()) {
					isAuthorized = true;
				}
				else if (result.responseBody() != null) {
					denialReasons.add(result.responseBody());
				}
			}
		}

		if (!isAuthorized) {
			String prefix = "Access Denied.\n";
			StringBuilder exceptionBody = new StringBuilder("Access Denied by the Cedar Policy Engine.\n\n");

			if (!denialReasons.isEmpty()) {
				for (String reason : denialReasons) {
					exceptionBody.append(reason.replaceAll("(?m)^" + prefix, "")).append("\n");
				}
			}
			else {
				exceptionBody.append("You do not have permission to add guardians to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		model.addAttribute("guardian", new Guardian());
		model.addAttribute("patientId", patientId);
		model.addAttribute("fromPatientForm", fromPatientForm);

		if (patientId != null || Boolean.TRUE.equals(fromPatientForm)) {
			model.addAttribute("authorities", this.authorities.findAll());
		}

		return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/guardians/new")
	public String processCreationForm(@Valid Guardian guardian, BindingResult result,
			@RequestParam(name = "patientId", required = false) Integer patientId,
			@RequestParam(name = "authorityId", required = false) Integer authorityId,
			@RequestParam(name = "fromPatientForm", required = false) Boolean fromPatientForm,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		// Check binding/validation errors.
		if (result.hasErrors()) {
			model.addAttribute("patientId", patientId);
			model.addAttribute("fromPatientForm", fromPatientForm);
			if (patientId != null || Boolean.TRUE.equals(fromPatientForm)) {
				model.addAttribute("authorities", this.authorities.findAll());
			}
			return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
		}

		// Check for empty clinics.
		Collection<Clinic> submittedClinics = guardian.getClinics();
		if (submittedClinics == null || submittedClinics.isEmpty()) {
			result.reject("clinicsRequired", "You must assign the Guardian to at least one valid Clinic.");
			model.addAttribute("patientId", patientId);
			model.addAttribute("fromPatientForm", fromPatientForm);
			if (patientId != null || Boolean.TRUE.equals(fromPatientForm)) {
				model.addAttribute("authorities", this.authorities.findAll());
			}
			return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
		}

		// Cedar authorization on submitted clinics.
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		for (Clinic clinic : submittedClinics) {
			if (clinic == null || clinic.getClinicName() == null)
				continue;
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var evalResult = cedarEvaluator.evaluate(principal, "AddGuardian", "Clinic", cedarClinicId, "Page");

			if (!evalResult.isGranted()) {
				isAuthorized = false;
				if (evalResult.responseBody() != null) {
					denialReasons.add(evalResult.responseBody());
				}
			}
		}

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
					.append("You do not have permission to add guardians to one or more of the selected clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		// Duplicate check.
		if (StringUtils.hasLength(guardian.getLastName()) && StringUtils.hasLength(guardian.getFirstName())
				&& guardian.isNew()) {
			boolean duplicateExists = guardians
				.findByLastNameStartingWith(guardian.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(a -> a.getFirstName().equalsIgnoreCase(guardian.getFirstName())
						&& Objects.equals(a.getBirthDate(), guardian.getBirthDate())
						&& Objects.equals(a.getGender(), guardian.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A guardian with this first and last name, birth date, and gender already exists.");
			}
		}

		// Final error check.
		if (result.hasErrors()) {
			model.addAttribute("patientId", patientId);
			model.addAttribute("fromPatientForm", fromPatientForm);
			if (patientId != null || Boolean.TRUE.equals(fromPatientForm)) {
				model.addAttribute("authorities", this.authorities.findAll());
			}
			return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
		}

		try {
			this.guardians.save(guardian);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("patientId", patientId);
			model.addAttribute("fromPatientForm", fromPatientForm);
			if (patientId != null || Boolean.TRUE.equals(fromPatientForm)) {
				model.addAttribute("authorities", this.authorities.findAll());
			}
			return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));

		// Existing Patient (from patientDetails.html).
		if (patientId != null && authorityId != null) {
			Patient patient = this.patients.findById(patientId)
				.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
			GuardianAuthority auth = this.authorities.findById(authorityId).orElse(null);
			if (auth != null) {
				PatientGuardian pa = new PatientGuardian(patient, guardian, auth);
				if (patient.getGuardians() == null) {
					patient.setGuardians(new java.util.LinkedHashSet<>());
				}
				patient.getGuardians().add(pa);
				this.patients.save(patient);
			}
			eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
			redirectAttributes.addFlashAttribute("message",
					"New Guardian has been created and assigned to the Patient.");
			return "redirect:/patients/" + patientId;
		}

		// New Patient (from createOrUpdatePatientForm.html via session stash).
		if (Boolean.TRUE.equals(fromPatientForm)) {
			PatientFormState state = (PatientFormState) session.getAttribute("patientFormState");
			if (state != null) {
				List<Integer> guardianIds = state.getGuardianIds();
				if (guardianIds == null) {
					guardianIds = new java.util.ArrayList<>();
				}
				guardianIds.add(guardian.getId());
				state.setGuardianIds(guardianIds);
				if (authorityId != null) {
					state.setAuthorityId(authorityId);
				}
				session.setAttribute("patientFormState", state);

				redirectAttributes.addFlashAttribute("message",
						"New Guardian created. Complete the Patient form to finalise.");

				if (state.getPatientId() != null) {
					session.removeAttribute("patientFormState");
					// Restore state via flash attributes for the edit path.
					redirectAttributes.addFlashAttribute("selectedGuardianIds", state.getGuardianIds());
					redirectAttributes.addFlashAttribute("selectedAuthorityId", state.getAuthorityId());
					redirectAttributes.addFlashAttribute("selectedDoctorIds", state.getDoctorIds());
					return "redirect:/patients/" + state.getPatientId() + "/edit";
				}
				return "redirect:/patients/new";
			}
		}

		// Standalone Guardian creation (default behaviour from
		// createOrUpdateGuardianForm.html).
		redirectAttributes.addFlashAttribute("message", "New Guardian has been added.");
		return "redirect:/guardians/" + guardian.getId();
	}

	/**
	 * Initializes the form for updating an existing Guardian entity.
	 */
	@GetMapping("/guardians/{guardianId}/edit")
	@CedarAuthorization(action = "EditGuardian", resourceType = "Guardian", validate = true)
	public String initUpdateForm(@PathVariable("guardianId") int guardianId, Model model) {
		Guardian guardian = this.guardians.findById(guardianId)
			.orElseThrow(() -> new IllegalArgumentException("Guardian not found with identifier: " + guardianId));
		model.addAttribute("guardian", guardian);
		return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes the submission of the Guardian update form.
	 */
	@PostMapping("/guardians/{guardianId}/edit")
	public String processUpdateForm(@Valid Guardian guardian, BindingResult result,
			@PathVariable("guardianId") int guardianId, RedirectAttributes redirectAttributes, HttpSession session,
			Model model) {

		// Check binding/validation errors.
		if (result.hasErrors()) {
			model.addAttribute("error", "There was an error in updating the guardian.");
			return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
		}

		// Cedar authorization: can the principal edit this specific guardian?
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Guardian existingGuardian = this.guardians.findById(guardianId)
			.orElseThrow(() -> new IllegalArgumentException("Guardian not found: " + guardianId));

		String resourceName = existingGuardian.getFirstName() + " " + existingGuardian.getLastName();
		var guardianEval = cedarEvaluator.evaluate(principal, "EditGuardian", "Guardian", resourceName, "Page");

		if (!guardianEval.isGranted()) {
			throw new CedarDeniedException("Access Denied: You do not have permission to edit this guardian.\n"
					+ (guardianEval.responseBody() != null ? guardianEval.responseBody() : ""));
		}

		// Check for empty clinics.
		Collection<Clinic> submittedClinics = guardian.getClinics();
		if (submittedClinics == null || submittedClinics.isEmpty()) {
			result.reject("clinicsRequired", "You must assign the Guardian to at least one valid Clinic.");
			model.addAttribute("error", "There was an error in updating the guardian.");
			return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
		}

		// Cedar authorization on submitted clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		for (Clinic clinic : submittedClinics) {
			if (clinic == null || clinic.getClinicName() == null)
				continue;
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			// Here we check for the "AddGuardian" action, instead of "EditGuardian",
			// since
			// the former applies to the "Clinic" resource.
			var clinicEval = cedarEvaluator.evaluate(principal, "AddGuardian", "Clinic", cedarClinicId, "Page");
			if (!clinicEval.isGranted()) {
				isAuthorized = false;
				if (clinicEval.responseBody() != null) {
					denialReasons.add(clinicEval.responseBody());
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

		if (existingGuardian.getClinics() != null) {
			for (Clinic existingClinic : existingGuardian.getClinics()) {
				if (existingClinic == null || existingClinic.getClinicName() == null)
					continue;
				String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Background");

				if (!viewEval.isGranted()) {
					finalClinics.add(existingClinic);
				}
			}
		}
		guardian.setClinics(finalClinics);

		// Duplicate check.
		if (StringUtils.hasLength(guardian.getLastName()) && StringUtils.hasLength(guardian.getFirstName())) {
			boolean duplicateExists = this.guardians
				.findByLastNameStartingWith(guardian.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(a -> a.getFirstName().equalsIgnoreCase(guardian.getFirstName())
						&& Objects.equals(a.getBirthDate(), guardian.getBirthDate())
						&& Objects.equals(a.getGender(), guardian.getGender())
						&& !Objects.equals(a.getId(), guardianId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A guardian with this first and last name, birth date, and gender already exists.");
			}
		}

		// Final error check.
		if (result.hasErrors()) {
			model.addAttribute("error", "There was an error in updating the guardian.");
			return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
		}

		guardian.setId(guardianId);

		try {
			this.guardians.save(guardian);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("error", "There was an error in updating the guardian.");
			return VIEWS_GUARDIAN_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "Guardian values updated.");
		return "redirect:/guardians/{guardianId}";
	}

}
