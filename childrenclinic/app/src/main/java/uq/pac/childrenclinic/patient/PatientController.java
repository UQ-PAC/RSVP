package uq.pac.childrenclinic.patient;

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
import uq.pac.childrenclinic.adult.Adult;
import uq.pac.childrenclinic.adult.AdultRepository;
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarRequest;
import uq.pac.childrenclinic.cedar.CedarService;
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

	private final CedarService cedarService;

	public PatientController(PatientRepository patients, GenderRepository genders, ClinicRepository clinics,
			AdultRepository adults, CedarService cedarService) {
		this.patients = patients;
		this.genders = genders;
		this.clinics = clinics;
		this.adults = adults;
		this.cedarService = cedarService;
	}

	@ModelAttribute("genders")
	public Collection<Gender> populateGenders() {
		return this.genders.findGenders();
	}

	@ModelAttribute("clinics")
	public Collection<Clinic> populateClinics() {
		return this.clinics.findClinics();
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

		EntityUID action = EntityUID.parse("ChildrenClinic::Action::\"ViewPatient\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));

		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Patient> authorized = allMatchingPatients.stream().filter(p -> {
			String resourceName = p.getFirstName() + " " + p.getLastName();
			EntityUID resource = EntityUID.parse("ChildrenClinic::Patient::\"" + resourceName + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

			String access = cedarService
				.checkAccess(new CedarRequest(principal, action, resource, new HashMap<>(), true))
				.getBody();
			authorizationMap.put(p.getId(), access);
			cedarResourceMap.put(p.getId(), "ChildrenClinic::Patient::\"" + resourceName + "\"");
			return access != null && access.startsWith("Access Granted.");
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingPatients.size() == 1) {
			return "redirect:/patients/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Patient> pageContent = start > authorized.size() ? List.of()
				: authorized.subList(start, end);
		Page<Patient> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listPatients", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", action.toString());
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
	@CedarAuthorization(action = "AddPatient", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initCreationForm(Model model) {
		model.addAttribute("patient", new Patient());
		return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/patients/new")
	@CedarAuthorization(action = "AddPatient", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processCreationForm(@Valid Patient patient, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.hasLength(patient.getLastName()) && StringUtils.hasLength(patient.getFirstName())
				&& patient.isNew()) {
			boolean duplicateExists = patients.findByLastNameStartingWith(patient.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(p -> p.getFirstName().equalsIgnoreCase(patient.getFirstName()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "A patient with this first and last name already exists");
			}
		}
		if (result.hasErrors())
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
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
	@CedarAuthorization(action = "EditPatient", resourceType = "Patient", validate = true)
	public String processUpdateForm(@Valid Patient patient, BindingResult result,
			@PathVariable("patientId") int patientId, RedirectAttributes redirectAttributes) {
		if (StringUtils.hasLength(patient.getLastName()) && StringUtils.hasLength(patient.getFirstName())) {
			boolean duplicateExists = this.patients
				.findByLastNameStartingWith(patient.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(p -> p.getFirstName().equalsIgnoreCase(patient.getFirstName())
						&& !Objects.equals(p.getId(), patientId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "A patient with this first and last name already exists.");
			}
		}

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the patient.");
			return VIEWS_PATIENT_CREATE_OR_UPDATE_FORM;
		}

		patient.setId(patientId);
		this.patients.save(patient);

		redirectAttributes.addFlashAttribute("message", "Patient Values Updated.");
		return "redirect:/patients/{patientId}";
	}

}
