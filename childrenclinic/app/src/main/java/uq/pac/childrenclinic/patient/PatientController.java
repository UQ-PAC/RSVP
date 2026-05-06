package uq.pac.childrenclinic.patient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import uq.pac.childrenclinic.adult.Adult;
import uq.pac.childrenclinic.adult.AdultRepository;
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarDeniedException;
import uq.pac.childrenclinic.cedar.CedarProgrammaticEvaluator;
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

	private final AdultRepository adults;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	public PatientController(PatientRepository patients, GenderRepository genders, ClinicRepository clinics,
			AdultRepository adults, CedarProgrammaticEvaluator cedarEvaluator) {
		this.patients = patients;
		this.genders = genders;
		this.clinics = clinics;
		this.adults = adults;
		this.cedarEvaluator = cedarEvaluator;
	}

	@ModelAttribute("genders")
	public Collection<Gender> populateGenders() {
		return this.genders.findGenders();
	}

	@ModelAttribute("clinics")
	public Collection<Clinic> populateClinics(HttpSession session) {
		Collection<Clinic> allClinics = this.clinics.findClinics();
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		return allClinics.stream().filter(clinic -> {
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var result = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Item");
			return result.isGranted();
		}).collect(Collectors.toList());
	}

	@ModelAttribute("adults")
	public Collection<Adult> populateAdults() {
		return this.adults.findAll();
	}

	@InitBinder("patient")
	public void initPatientBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/patients/find")
	@CedarAuthorization(action = "ListPatients", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initFindForm(Model model) {
		model.addAttribute("patient", new Patient());
		return "patients/findPatients";
	}

	@GetMapping("/patients")
	@CedarAuthorization(action = "ListPatients", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Patient patient, BindingResult result,
			Model model, HttpSession session) {
		String lastName = patient.getLastName() == null ? "" : patient.getLastName();

		List<Patient> allMatchingPatients = this.patients.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingPatients.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No patients found.");
			return "patients/findPatients";
		}

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Patient> authorized = allMatchingPatients.stream().filter(p -> {
			String resourceName = p.getFirstName() + " " + p.getLastName();
			var evalResult = cedarEvaluator.evaluate(principal, "ViewPatient", "Patient", resourceName, "Item");
			authorizationMap.put(p.getId(), evalResult.responseBody());
			cedarResourceMap.put(p.getId(), "ChildrenClinic::Patient::\"" + resourceName + "\"");
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
	public ModelAndView showPatient(@PathVariable("patientId") int patientId) {
		ModelAndView mav = new ModelAndView("patients/patientDetails");
		Patient patient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found for identifier: " + patientId));
		mav.addObject("patient", patient);
		return mav;
	}

	@GetMapping("/patients/new")
	public String initCreationForm(Model model, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Collection<Clinic> allClinics = this.clinics.findClinics();

		// Evaluate Cedar for each Clinic and log the result.
		boolean isAuthorized = false;
		List<String> denialReasons = new ArrayList<>();

		for (Clinic clinic : allClinics) {
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var result = cedarEvaluator.evaluate(principal, "AddPatient", "Clinic", cedarClinicId, "Page");

			if (result.isGranted()) {
				isAuthorized = true;
			}
			else if (result.responseBody() != null) {
				denialReasons.add(result.responseBody());
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
				exceptionBody.append("You do not have permission to add patients to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		model.addAttribute("patient", new Patient());
		return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/patients/new")
	public String processCreationForm(@Valid Patient patient, BindingResult result,
			RedirectAttributes redirectAttributes, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		// Evaluate Cedar for all the submitted Clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		Collection<Clinic> submittedClinics = patient.getClinics();

		if (submittedClinics == null || submittedClinics.isEmpty()) {
			isAuthorized = false;
			denialReasons.add("You must assign the Patient to at least one valid Clinic.");
		}
		else {
			for (Clinic clinic : submittedClinics) {
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
					.append("You do not have permission to add patients to one or more of the selected clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		if (StringUtils.hasLength(patient.getLastName()) && StringUtils.hasLength(patient.getFirstName())
				&& patient.isNew()) {
			boolean duplicateExists = patients.findByLastNameStartingWith(patient.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(p -> p.getFirstName().equalsIgnoreCase(patient.getFirstName())
						&& p.getBirthDate().equals(patient.getBirthDate())
						&& p.getGender().equals(patient.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "A patient with this first and last name already exists");
			}
		}

		if (result.hasErrors()) {
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		this.patients.save(patient);
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
	public String initUpdateForm(@PathVariable("patientId") int patientId, Model model) {
		Patient patient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient entity not found for identifier: " + patientId));
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
			@PathVariable("patientId") int patientId, RedirectAttributes redirectAttributes, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Patient existingPatient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

		String resourceName = existingPatient.getFirstName() + " " + existingPatient.getLastName();
		var patientEval = cedarEvaluator.evaluate(principal, "EditPatient", "Patient", resourceName, "Page");

		if (!patientEval.isGranted()) {
			throw new CedarDeniedException("Access Denied: You do not have permission to edit this patient.\n"
					+ (patientEval.responseBody() != null ? patientEval.responseBody() : ""));
		}

		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();
		Collection<Clinic> submittedClinics = patient.getClinics();

		if (submittedClinics != null) {
			for (Clinic clinic : submittedClinics) {
				String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				// Here we check for the "AddPatient" action, instead of "EditPatient", since the former applies to the "Clinic" resource.
				var clinicEval = cedarEvaluator.evaluate(principal, "AddPatient", "Clinic", cedarClinicId, "Page");
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

		if (StringUtils.hasLength(patient.getLastName()) && StringUtils.hasLength(patient.getFirstName())) {
			boolean duplicateExists = this.patients
				.findByLastNameStartingWith(patient.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(p -> p.getFirstName().equalsIgnoreCase(patient.getFirstName())
						&& p.getBirthDate().equals(patient.getBirthDate()) && p.getGender().equals(patient.getGender())
						&& !Objects.equals(p.getId(), patientId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "A patient with this first and last name already exists.");
			}
		}

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the patient.");
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		Set<Clinic> finalClinics = new HashSet<>(submittedClinics != null ? submittedClinics : new ArrayList<>());

		for (Clinic existingClinic : existingPatient.getClinics()) {
			String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Background");

			// If the user did NOT have permission to view this clinic, it means it wasn't
			// in the form.
			// We must re-add it to the final payload to prevent it from being deleted.
			if (!viewEval.isGranted()) {
				finalClinics.add(existingClinic);
			}
		}
		patient.setClinics(finalClinics);

		patient.setId(patientId);
		this.patients.save(patient);
		redirectAttributes.addFlashAttribute("message", "Patient values updated.");
		return "redirect:/patients/{patientId}";
	}

}
