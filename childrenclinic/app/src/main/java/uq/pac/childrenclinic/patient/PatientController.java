package uq.pac.childrenclinic.patient;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

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

import uq.pac.childrenclinic.adult.Adult;
import uq.pac.childrenclinic.adult.AdultRepository;
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarDeniedException;
import uq.pac.childrenclinic.cedar.CedarEntitiesInvalidationEvent;
import uq.pac.childrenclinic.cedar.CedarProgrammaticEvaluator;
import uq.pac.childrenclinic.doctor.Doctor;
import uq.pac.childrenclinic.doctor.DoctorRepository;
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

	private final AdultAuthorityRepository authorities;

	private final DoctorRepository doctorRepository;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	private final ApplicationEventPublisher eventPublisher;

	public PatientController(PatientRepository patients, GenderRepository genders, ClinicRepository clinics,
			AdultRepository adults, AdultAuthorityRepository authorities, DoctorRepository doctorRepository,
			ApplicationEventPublisher eventPublisher,
			CedarProgrammaticEvaluator cedarEvaluator) {
		this.patients = patients;
		this.genders = genders;
		this.clinics = clinics;
		this.adults = adults;
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

		if (allClinics == null) return new ArrayList<>();

		return allClinics.stream().filter(clinic -> {
			if (clinic == null || clinic.getClinicName() == null) return false;
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var result = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Item");
			return result.isGranted();
		}).collect(Collectors.toList());
	}

	@ModelAttribute("adults")
	public Collection<Adult> populateAdults() {
		return this.adults.findAll();
	}

	@ModelAttribute("authorities")
    public Collection<AdultAuthority> populateAuthorities() {
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

		if (allMatchingPatients == null || allMatchingPatients.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No patients found.");
			return "patients/findPatients";
		}

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Patient> authorized = allMatchingPatients.stream().filter(p -> {
			if (p == null) return false;
			String resourceName = (p.getFirstName() != null ? p.getFirstName() : "") + " " + 
			                      (p.getLastName() != null ? p.getLastName() : "");
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

		if (allClinics != null) {
			for (Clinic clinic : allClinics) {
				if (clinic != null && clinic.getClinicName() != null) {
					String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
					var result = cedarEvaluator.evaluate(principal, "AddPatient", "Clinic", cedarClinicId, "Page");

					if (result.isGranted()) {
						isAuthorized = true;
					} else if (result.responseBody() != null) {
						denialReasons.add(result.responseBody());
					}
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
				exceptionBody.append("You do not have permission to add patients to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		model.addAttribute("patient", new Patient());
		return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/patients/new")
	public String processCreationForm(@Valid Patient patient, BindingResult result,
			@RequestParam(name = "clinics", required = false) Collection<Clinic> submittedClinics,
			@RequestParam(name = "newAdultIds", required = false) List<Integer> adultIds,
			@RequestParam(name = "newAuthorityId", required = false) Integer authorityId,
			@RequestParam(name = "newDoctorIds", required = false) List<Integer> doctorIds,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		// Evaluate Cedar for all the submitted Clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		if (submittedClinics == null || submittedClinics.isEmpty()) {
			isAuthorized = false;
			denialReasons.add("You must assign the Patient to at least one valid Clinic.");
		}
		else {
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
		}

		if (patient.getResponsibleAdults() == null) {
			patient.setResponsibleAdults(new LinkedHashSet<>());
		}
		if (patient.getClinics() == null) {
			patient.setClinics(new HashSet<>());
		}

		if (submittedClinics != null) {
			patient.getClinics().addAll(submittedClinics);
		}

		if (adultIds != null && authorityId != null) {
			AdultAuthority auth = authorities.findById(authorityId).orElse(null);
			if (auth != null) {
				for (Integer adId : adultIds) {
					if (adId != null) {
						Adult adult = adults.findById(adId).orElse(null);
						if (adult != null) {
							PatientAdult pa = new PatientAdult();
							pa.setPatient(patient);
							pa.setAdult(adult);
							pa.setAuthority(auth);
							patient.getResponsibleAdults().add(pa);
						}
					}
				}
			}
		}

		if (patient.getResponsibleAdults() == null || patient.getResponsibleAdults().isEmpty()) {
			isAuthorized = false;
			denialReasons.add("You must assign the Patient to at least one Responsible Adult.");
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
				.anyMatch(p -> p != null && p.getFirstName() != null
						&& p.getFirstName().equalsIgnoreCase(patient.getFirstName())
						&& Objects.equals(p.getBirthDate(), patient.getBirthDate())
						&& Objects.equals(p.getGender(), patient.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "A patient with this first and last name, birth date, and gender already exists.");
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("selectedAdultIds", adultIds);
            model.addAttribute("selectedAuthorityId", authorityId);
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		try {
			this.patients.save(patient);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("selectedAdultIds", adultIds);
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
	public String initUpdateForm(@PathVariable("patientId") int patientId, Model model) {
		Patient patient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient entity not found for identifier: " + patientId));
		
		if (patient.getResponsibleAdults() != null && !patient.getResponsibleAdults().isEmpty()) {
            List<Integer> adultIds = patient.getResponsibleAdults().stream()
                    .map(pa -> pa.getAdult().getId())
                    .collect(Collectors.toList());
            model.addAttribute("selectedAdultIds", adultIds);

            Integer authId = patient.getResponsibleAdults().iterator().next().getAuthority().getId();
            model.addAttribute("selectedAuthorityId", authId);
        }

		if (patient.getDoctors() != null && !patient.getDoctors().isEmpty()) {
            List<Integer> selectedDoctorIds = patient.getDoctors().stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toList());
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
			@RequestParam(name = "newAdultIds", required = false) List<Integer> adultIds,
			@RequestParam(name = "newAuthorityId", required = false) Integer authorityId,
			@RequestParam(name = "newDoctorIds", required = false) List<Integer> doctorIds,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Patient existingPatient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

		String resourceName = (existingPatient.getFirstName() != null ? existingPatient.getFirstName() : "") + " " + 
		                      (existingPatient.getLastName() != null ? existingPatient.getLastName() : "");
		var patientEval = cedarEvaluator.evaluate(principal, "EditPatient", "Patient", resourceName, "Page");

		if (!patientEval.isGranted()) {
			throw new CedarDeniedException("Access Denied: You do not have permission to edit this patient.\n"
					+ (patientEval.responseBody() != null ? patientEval.responseBody() : ""));
		}

		// Evaluate Cedar for all the submitted Clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		if (submittedClinics != null && !submittedClinics.isEmpty()) {
			for (Clinic clinic : submittedClinics) {
				if (clinic != null && clinic.getClinicName() != null) {
					String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
					// Here we check for the "AddPatient" action, instead of "EditPatient",
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

		Set<Clinic> finalClinics = new HashSet<>(submittedClinics != null ? submittedClinics : new ArrayList<>());

		if (existingPatient.getClinics() != null) {
			for (Clinic existingClinic : existingPatient.getClinics()) {
				if (existingClinic != null && existingClinic.getClinicName() != null) {
					String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
					var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Background");

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

		// Carry over the existing responsible adults.
		Set<PatientAdult> mergedAdults;
		if (existingPatient.getResponsibleAdults() != null) {
			mergedAdults = new LinkedHashSet<>(existingPatient.getResponsibleAdults());
		} else {
			mergedAdults = new LinkedHashSet<>();
		}

		// Collect the adult IDs already present to prevent duplicates.
		Set<Integer> existingAdultIds = mergedAdults.stream()
				.map(pa -> pa.getAdult().getId())
				.collect(Collectors.toSet());

		// Add only newly submitted adults that are not already in the set.
		if (adultIds != null && authorityId != null) {
			AdultAuthority auth = authorities.findById(authorityId).orElse(null);
			if (auth != null) {
				for (Integer adId : adultIds) {
					if (adId != null && !existingAdultIds.contains(adId)) {
						Adult adult = adults.findById(adId).orElse(null);
						if (adult != null) {
							PatientAdult pa = new PatientAdult(existingPatient, adult, auth);
							mergedAdults.add(pa);
						}
					}
				}
			}
		}

		patient.setResponsibleAdults(mergedAdults);

		if (patient.getResponsibleAdults() == null || patient.getResponsibleAdults().isEmpty()) {
			isAuthorized = false;
			denialReasons.add("You must assign the Patient to at least one Responsible Adult.");
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

		// Deny Access if any checks failed.
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
				.anyMatch(p -> p != null && p.getFirstName() != null &&
							p.getFirstName().equalsIgnoreCase(patient.getFirstName()) &&
							Objects.equals(p.getBirthDate(), patient.getBirthDate()) && 
							Objects.equals(p.getGender(), patient.getGender()) &&
							!Objects.equals(p.getId(), patientId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "A patient with this first and last name, birth date, and gender already exists.");
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("selectedAdultIds", adultIds);
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
			model.addAttribute("selectedAdultIds", adultIds);
			model.addAttribute("selectedAuthorityId", authorityId);
			model.addAttribute("error", "There was an error in updating the patient.");
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "Patient values updated.");
		return "redirect:/patients/{patientId}";
	}

}
