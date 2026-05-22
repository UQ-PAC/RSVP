package uq.pac.childrenclinic.patient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import uq.pac.childrenclinic.doctor.Doctor;
import uq.pac.childrenclinic.doctor.DoctorRepository;
import uq.pac.childrenclinic.guardian.Guardian;
import uq.pac.childrenclinic.guardian.GuardianRepository;
import uq.pac.childrenclinic.model.Gender;
import uq.pac.childrenclinic.model.GenderRepository;
import uq.pac.childrenclinic.system.Clinic;
import uq.pac.childrenclinic.system.ClinicRepository;

@Controller
public class PatientController {

	private static final String VIEWS_PATIENT_CREATE_OR_UPDATE_FORM = "patients/createOrUpdatePatientForm";

	private final PatientRepository patients;

	private final GenderRepository genders;

	private final ClinicRepository clinics;

	private final GuardianRepository guardians;

	private final GuardianAuthorityRepository authorities;

	private final DoctorRepository doctorRepository;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	private final ApplicationEventPublisher eventPublisher;

	public PatientController(PatientRepository patients, GenderRepository genders, ClinicRepository clinics,
			GuardianRepository guardians, GuardianAuthorityRepository authorities, DoctorRepository doctorRepository,
			ApplicationEventPublisher eventPublisher, CedarProgrammaticEvaluator cedarEvaluator) {
		this.patients = patients;
		this.genders = genders;
		this.clinics = clinics;
		this.guardians = guardians;
		this.authorities = authorities;
		this.doctorRepository = doctorRepository;
		this.cedarEvaluator = cedarEvaluator;
		this.eventPublisher = eventPublisher;
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

	@ModelAttribute("guardians")
	public Collection<Guardian> populateGuardians() {
		return this.guardians.findAll();
	}

	@ModelAttribute("authorities")
	public Collection<GuardianAuthority> populateAuthorities() {
		return this.authorities.findAll();
	}

	@ModelAttribute("doctors")
	public Collection<Doctor> populateDoctors() {
		return this.doctorRepository.findAll();
	}

	@InitBinder("patient")
	public void initPatientBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/patients")
	@CedarAuthorization(action = "ListPatients", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Patient patient, BindingResult result,
			Model model, HttpSession session) {
		String lastName = patient.getLastName() == null ? "" : patient.getLastName();

		List<Patient> allMatchingPatients = this.patients.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingPatients == null || allMatchingPatients.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No patients found.");
			return "redirect:/?error=noPatients&query=" + UriUtils.encode(lastName, StandardCharsets.UTF_8);
		}

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Patient> authorized = allMatchingPatients.stream().filter(p -> {
			if (p == null)
				return false;
			String resourceName = (p.getFirstName() != null ? p.getFirstName() : "") + " "
					+ (p.getLastName() != null ? p.getLastName() : "");
			var evalResult = cedarEvaluator.evaluate(principal, "ViewPatient", "Patient", resourceName.trim(), "Item");
			authorizationMap.put(p.getId(), evalResult.responseBody());
			cedarResourceMap.put(p.getId(), "ChildrenClinic::Patient::\"" + resourceName.trim() + "\"");
			return evalResult.isGranted();
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingPatients.size() == 1) {
			return "redirect:/patients/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Patient> pageContent = start > authorized.size() ? List.of() : authorized.subList(start, end);
		Page<Patient> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listPatients", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", "ChildrenClinic::Action::\"ViewPatient\"");
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		return "patients/patientsList";
	}

	@GetMapping("/patients/{patientId}")
	@CedarAuthorization(action = "ViewPatient", resourceType = "Patient", validate = true)
	public ModelAndView showPatient(@PathVariable("patientId") int patientId, HttpSession session) {
		ModelAndView mav = new ModelAndView("patients/patientDetails");
		Patient patient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found for identifier: " + patientId));
		mav.addObject("patient", patient);

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		String resourceName = (patient.getFirstName() != null ? patient.getFirstName() : "") + " "
				+ (patient.getLastName() != null ? patient.getLastName() : "");
		var editEval = cedarEvaluator.evaluate(principal, "EditPatient", "Patient", resourceName.trim(), "Background");
		mav.addObject("canEdit", editEval.isGranted());

		var addVisitEval = cedarEvaluator.evaluate(principal, "EditPatient", "Patient", resourceName.trim(),
				"Background");
		mav.addObject("canAddVisit", addVisitEval.isGranted());

		return mav;
	}

	@GetMapping("/patients/new")
	public String initCreationForm(Model model, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Collection<Clinic> allClinics = this.clinics.findClinics();

		boolean isAuthorized = false;
		List<String> denialReasons = new ArrayList<>();

		if (allClinics != null) {
			for (Clinic clinic : allClinics) {
				if (clinic != null && clinic.getClinicName() != null) {
					String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
					var result = cedarEvaluator.evaluate(principal, "AddPatient", "Clinic", cedarClinicId, "Page");

					if (result.isGranted()) {
						isAuthorized = true;
					}
					else if (result.responseBody() != null) {
						denialReasons.add(result.responseBody());
					}
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
				exceptionBody.append("You do not have permission to add patients to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		PatientFormState state = (PatientFormState) session.getAttribute("patientFormState");

		if (state != null) {
			Patient patient = new Patient();
			patient.setFirstName(state.getFirstName());
			patient.setLastName(state.getLastName());
			patient.setAddress(state.getAddress());
			patient.setCity(state.getCity());
			if (state.getBirthDate() != null && !state.getBirthDate().isBlank()) {
				patient.setBirthDate(java.time.LocalDate.parse(state.getBirthDate()));
			}
			if (state.getGender() != null) {
				this.genders.findGenders()
					.stream()
					.filter(g -> g.getName().equals(state.getGender()))
					.findFirst()
					.ifPresent(patient::setGender);
			}
			if (state.getClinics() != null) {
				Set<Clinic> selectedClinics = new java.util.HashSet<>();
				for (String clinicName : state.getClinics()) {
					this.clinics.findByName(clinicName).ifPresent(selectedClinics::add);
				}
				patient.setClinics(selectedClinics);
			}
			model.addAttribute("patient", patient);
			model.addAttribute("selectedGuardianIds", state.getGuardianIds());
			model.addAttribute("selectedAuthorityId", state.getAuthorityId());
			model.addAttribute("selectedDoctorIds", state.getDoctorIds());

			// Clear the session state after restoration.
			session.removeAttribute("patientFormState");
		}
		else {
			model.addAttribute("patient", new Patient());
		}

		return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/patients/new")
	public String processCreationForm(@Valid Patient patient, BindingResult result,
			@RequestParam(name = "clinics", required = false) Collection<Clinic> submittedClinics,
			@RequestParam(name = "newGuardianIds", required = false) List<Integer> guardianIds,
			@RequestParam(name = "newAuthorityId", required = false) Integer authorityId,
			@RequestParam(name = "newDoctorIds", required = false) List<Integer> doctorIds,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		// Check binding/validation errors.
		if (result.hasErrors()) {
			model.addAttribute("selectedGuardianIds", guardianIds);
			model.addAttribute("selectedAuthorityId", authorityId);
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		// Check for empty clinics.
		if (submittedClinics == null || submittedClinics.isEmpty()) {
			result.reject("clinicsRequired", "You must assign the Patient to at least one valid Clinic.");
		}

		// Build clinics.
		if (patient.getClinics() == null) {
			patient.setClinics(new HashSet<>());
		}

		if (submittedClinics != null) {
			patient.getClinics().addAll(submittedClinics);
		}

		// Build guardians.
		if (patient.getGuardians() == null) {
			patient.setGuardians(new LinkedHashSet<>());
		}

		if (guardianIds != null && authorityId != null) {
			GuardianAuthority auth = authorities.findById(authorityId).orElse(null);
			if (auth != null) {
				for (Integer adId : guardianIds) {
					if (adId != null) {
						Guardian guardian = guardians.findById(adId).orElse(null);
						if (guardian != null) {
							PatientGuardian pa = new PatientGuardian();
							pa.setPatient(patient);
							pa.setGuardian(guardian);
							pa.setAuthority(auth);
							patient.getGuardians().add(pa);
						}
					}
				}
			}
		}

		if (patient.getGuardians() == null || patient.getGuardians().isEmpty()) {
			result.reject("guardiansRequired", "You must assign the Patient to at least one Guardian.");
		}

		// Build doctors.
		if (doctorIds != null && !doctorIds.isEmpty()) {
			Set<Doctor> selectedDoctors = new LinkedHashSet<>();
			for (Integer docId : doctorIds) {
				if (docId != null) {
					doctorRepository.findById(docId).ifPresent(selectedDoctors::add);
				}
			}
			patient.setDoctors(selectedDoctors);
		}

		if (patient.getDoctors() == null || patient.getDoctors().isEmpty()) {
			result.reject("doctorsRequired", "You must assign the Patient to at least one Doctor.");
		}

		// Return the form if any validation errors accumulated so far.
		if (result.hasErrors()) {
			model.addAttribute("selectedGuardianIds", guardianIds);
			model.addAttribute("selectedAuthorityId", authorityId);
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		// Cedar authorization on submitted clinics.
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		for (Clinic clinic : submittedClinics) {
			if (clinic != null && clinic.getClinicName() != null) {
				String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var evalResult = cedarEvaluator.evaluate(principal, "AddPatient", "Clinic", cedarClinicId, "Page");

				if (!evalResult.isGranted()) {
					isAuthorized = false;
					if (evalResult.responseBody() != null) {
						denialReasons.add(evalResult.responseBody());
					}
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
					.append("You do not have permission to add patients to one or more of the selected clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		// Duplicate check.
		if (StringUtils.hasLength(patient.getLastName()) && StringUtils.hasLength(patient.getFirstName())
				&& patient.isNew()) {
			boolean duplicateExists = patients.findByLastNameStartingWith(patient.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(p -> p != null && p.getFirstName() != null
						&& p.getFirstName().equalsIgnoreCase(patient.getFirstName())
						&& Objects.equals(p.getBirthDate(), patient.getBirthDate())
						&& Objects.equals(p.getGender(), patient.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A patient with this first and last name, birth date, and gender already exists.");
			}
		}

		// Final error check.
		if (result.hasErrors()) {
			model.addAttribute("selectedGuardianIds", guardianIds);
			model.addAttribute("selectedAuthorityId", authorityId);
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		try {
			this.patients.save(patient);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("selectedGuardianIds", guardianIds);
			model.addAttribute("selectedAuthorityId", authorityId);
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "New Patient has been added.");
		return "redirect:/patients/" + patient.getId();
	}

	/**
	 * Initializes the form for updating an existing Patient entity. * @param patientId
	 * The primary key identifier of the Patient.
	 * @param model The Spring MVC model payload.
	 * @return The logical view name for the creation/update form.
	 * @throws IllegalArgumentException if the requested entity identifier does not exist.
	 */
	@GetMapping("/patients/{patientId}/edit")
	@CedarAuthorization(action = "EditPatient", resourceType = "Patient", validate = true)
	public String initUpdateForm(@PathVariable("patientId") int patientId, Model model, HttpSession session) {
		session.removeAttribute("patientFormState");

		Patient patient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient entity not found for identifier: " + patientId));

		List<Integer> guardianIds = new ArrayList<>();
		Integer authId = null;
		if (patient.getGuardians() != null && !patient.getGuardians().isEmpty()) {
			guardianIds = patient.getGuardians()
				.stream()
				.map(pa -> pa.getGuardian().getId())
				.collect(Collectors.toList());
			authId = patient.getGuardians().iterator().next().getAuthority().getId();
		}

		Map<String, ?> flashMap = model.asMap();
		if (flashMap.containsKey("selectedGuardianIds")) {
			@SuppressWarnings("unchecked")
			List<Integer> flashGuardianIds = (List<Integer>) flashMap.get("selectedGuardianIds");
			if (flashGuardianIds != null) {
				for (Integer id : flashGuardianIds) {
					if (id != null && !guardianIds.contains(id)) {
						guardianIds.add(id);
					}
				}
			}
		}
		if (flashMap.containsKey("selectedAuthorityId")) {
			authId = (Integer) flashMap.get("selectedAuthorityId");
		}

		model.addAttribute("selectedGuardianIds", guardianIds);
		model.addAttribute("selectedAuthorityId", authId);

		if (patient.getDoctors() != null && !patient.getDoctors().isEmpty()) {
			List<Integer> selectedDoctorIds = patient.getDoctors()
				.stream()
				.map(Doctor::getId)
				.collect(Collectors.toList());
			if (flashMap.containsKey("selectedDoctorIds")) {
				@SuppressWarnings("unchecked")
				List<Integer> flashDoctorIds = (List<Integer>) flashMap.get("selectedDoctorIds");
				if (flashDoctorIds != null) {
					for (Integer id : flashDoctorIds) {
						if (id != null && !selectedDoctorIds.contains(id)) {
							selectedDoctorIds.add(id);
						}
					}
				}
			}
			model.addAttribute("selectedDoctorIds", selectedDoctorIds);
		}

		model.addAttribute("patient", patient);
		return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes the submission of the Patient update form. * @param patient The
	 * data-bound Patient entity containing the updated state.
	 * @param result The binding result containing validation errors.
	 * @param patientId The primary key identifier from the request URI.
	 * @param redirectAttributes Attributes utilized for flash messaging upon redirection.
	 * @return The logical view name or redirection directive.
	 */
	@PostMapping("/patients/{patientId}/edit")
	public String processUpdateForm(@Valid Patient patient, BindingResult result,
			@PathVariable("patientId") int patientId,
			@RequestParam(name = "clinics", required = false) Collection<Clinic> submittedClinics,
			@RequestParam(name = "newGuardianIds", required = false) List<Integer> guardianIds,
			@RequestParam(name = "newAuthorityId", required = false) Integer authorityId,
			@RequestParam(name = "newDoctorIds", required = false) List<Integer> doctorIds,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		// Check binding/validation errors.
		if (result.hasErrors()) {
			model.addAttribute("selectedGuardianIds", guardianIds);
			model.addAttribute("selectedAuthorityId", authorityId);
			model.addAttribute("error", "There was an error in updating the patient.");
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		// Cedar authorization: can the principal edit this specific patient?
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Patient existingPatient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

		String resourceName = (existingPatient.getFirstName() != null ? existingPatient.getFirstName() : "") + " "
				+ (existingPatient.getLastName() != null ? existingPatient.getLastName() : "");
		var patientEval = cedarEvaluator.evaluate(principal, "EditPatient", "Patient", resourceName, "Page");

		if (!patientEval.isGranted()) {
			throw new CedarDeniedException("Access Denied: You do not have permission to edit this patient.\n"
					+ (patientEval.responseBody() != null ? patientEval.responseBody() : ""));
		}

		// Cedar authorization on submitted clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		if (submittedClinics != null && !submittedClinics.isEmpty()) {
			for (Clinic clinic : submittedClinics) {
				if (clinic != null && clinic.getClinicName() != null) {
					String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
					// Here we check for the "AddPatient" action, instead of
					// "EditPatient",
					// since the former applies to the "Clinic" resource.
					var clinicEval = cedarEvaluator.evaluate(principal, "AddPatient", "Clinic", cedarClinicId, "Page");
					if (!clinicEval.isGranted()) {
						isAuthorized = false;
						if (clinicEval.responseBody() != null) {
							denialReasons.add(clinicEval.responseBody());
						}
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

		if (existingPatient.getClinics() != null) {
			for (Clinic existingClinic : existingPatient.getClinics()) {
				if (existingClinic != null && existingClinic.getClinicName() != null) {
					String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
					var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId,
							"Background");

					if (!viewEval.isGranted()) {
						finalClinics.add(existingClinic);
					}
				}
			}
		}
		patient.setClinics(finalClinics);

		if (patient.getClinics() == null || patient.getClinics().isEmpty()) {
			result.rejectValue("clinics", "NotEmpty", "At least one clinic must be assigned.");
		}

		// Carry over the existing guardians.
		Set<PatientGuardian> mergedGuardians;
		if (existingPatient.getGuardians() != null) {
			mergedGuardians = new LinkedHashSet<>(existingPatient.getGuardians());
		}
		else {
			mergedGuardians = new LinkedHashSet<>();
		}

		// Collect the guardian IDs already present to prevent duplicates.
		Set<Integer> existingGuardianIds = mergedGuardians.stream()
			.map(pa -> pa.getGuardian().getId())
			.collect(Collectors.toSet());

		// Add only newly submitted guardians that are not already in the set.
		if (guardianIds != null && authorityId != null) {
			GuardianAuthority auth = authorities.findById(authorityId).orElse(null);
			if (auth != null) {
				for (Integer adId : guardianIds) {
					if (adId != null && !existingGuardianIds.contains(adId)) {
						Guardian guardian = guardians.findById(adId).orElse(null);
						if (guardian != null) {
							PatientGuardian pa = new PatientGuardian(existingPatient, guardian, auth);
							mergedGuardians.add(pa);
						}
					}
				}
			}
		}

		patient.setGuardians(mergedGuardians);

		if (patient.getGuardians() == null || patient.getGuardians().isEmpty()) {
			isAuthorized = false;
			denialReasons.add("You must assign the Patient to at least one Guardian.");
		}

		if (doctorIds != null && !doctorIds.isEmpty()) {
			Set<Doctor> selectedDoctors = new LinkedHashSet<>();
			for (Integer docId : doctorIds) {
				if (docId != null) {
					doctorRepository.findById(docId).ifPresent(selectedDoctors::add);
				}
			}
			patient.setDoctors(selectedDoctors);
		}

		if (patient.getDoctors() == null || patient.getDoctors().isEmpty()) {
			result.reject("doctorsRequired", "You must assign the Patient to at least one Doctor.");
		}

		// Duplicate check.
		if (StringUtils.hasLength(patient.getLastName()) && StringUtils.hasLength(patient.getFirstName())) {
			boolean duplicateExists = this.patients
				.findByLastNameStartingWith(patient.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(p -> p != null && p.getFirstName() != null
						&& p.getFirstName().equalsIgnoreCase(patient.getFirstName())
						&& Objects.equals(p.getBirthDate(), patient.getBirthDate())
						&& Objects.equals(p.getGender(), patient.getGender()) && !Objects.equals(p.getId(), patientId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A patient with this first and last name, birth date, and gender already exists.");
			}
		}

		// Final error check.
		if (result.hasErrors()) {
			model.addAttribute("selectedGuardianIds", guardianIds);
			model.addAttribute("selectedAuthorityId", authorityId);
			model.addAttribute("error", "There was an error in updating the patient.");
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		patient.setId(patientId);

		try {
			this.patients.save(patient);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("selectedGuardianIds", guardianIds);
			model.addAttribute("selectedAuthorityId", authorityId);
			model.addAttribute("error", "There was an error in updating the patient.");
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "Patient values updated.");
		return "redirect:/patients/{patientId}";
	}

	@PostMapping("/patients/stash-and-add-guardian")
	public String stashPatientFormAndRedirectToGuardianCreation(
			@RequestParam(name = "patientId", required = false) Integer patientId,
			@RequestParam(name = "firstName", required = false) String firstName,
			@RequestParam(name = "lastName", required = false) String lastName,
			@RequestParam(name = "address", required = false) String address,
			@RequestParam(name = "city", required = false) String city,
			@RequestParam(name = "birthDate", required = false) String birthDate,
			@RequestParam(name = "gender", required = false) String gender,
			@RequestParam(name = "clinics", required = false) List<String> clinicNames,
			@RequestParam(name = "newGuardianIds", required = false) List<Integer> guardianIds,
			@RequestParam(name = "newAuthorityId", required = false) Integer authorityId,
			@RequestParam(name = "newDoctorIds", required = false) List<Integer> doctorIds, HttpSession session) {

		PatientFormState state = new PatientFormState();
		state.setPatientId(patientId);
		state.setFirstName(firstName);
		state.setLastName(lastName);
		state.setAddress(address);
		state.setCity(city);
		state.setBirthDate(birthDate);
		state.setGender(gender);
		state.setClinics(clinicNames);
		state.setGuardianIds(guardianIds);
		state.setAuthorityId(authorityId);
		state.setDoctorIds(doctorIds);

		session.setAttribute("patientFormState", state);

		return "redirect:/guardians/new?fromPatientForm=true";
	}

}
