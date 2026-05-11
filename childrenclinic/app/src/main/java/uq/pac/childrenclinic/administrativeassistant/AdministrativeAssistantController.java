package uq.pac.childrenclinic.administrativeassistant;

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

@Controller
public class AdministrativeAssistantController {

	private static final String VIEWS_ADMINISTRATIVE_ASSISTANT_CREATE_OR_UPDATE_FORM = "administrative-assistants/createOrUpdateAdministrativeAssistantForm";

	private final AdministrativeAssistantRepository administrativeAssistants;

	private final GenderRepository genders;

	private final ClinicRepository clinics;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	private final ApplicationEventPublisher eventPublisher;

	public AdministrativeAssistantController(AdministrativeAssistantRepository administrativeAssistants,
			GenderRepository genders, ClinicRepository clinics, CedarProgrammaticEvaluator cedarEvaluator,
			ApplicationEventPublisher eventPublisher) {
		this.administrativeAssistants = administrativeAssistants;
		this.genders = genders;
		this.clinics = clinics;
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

	@InitBinder("administrativeAssistant")
	public void initAdministrativeAssistantBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/administrative-assistants/find")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initFindForm(Model model) {
		model.addAttribute("administrativeAssistant", new AdministrativeAssistant());
		return "administrative-assistants/findAdministrativeAssistants";
	}

	@GetMapping("/administrative-assistants")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processFindForm(@RequestParam(defaultValue = "1") int page,
			AdministrativeAssistant administrativeAssistant, BindingResult result, Model model, HttpSession session) {
		String lastName = administrativeAssistant.getLastName() == null ? "" : administrativeAssistant.getLastName();

		List<AdministrativeAssistant> allMatchingAssistants = this.administrativeAssistants
			.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingAssistants.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No administrative assistants found.");
			return "administrative-assistants/findAdministrativeAssistants";
		}

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<AdministrativeAssistant> authorized = allMatchingAssistants.stream().filter(s -> {
			String resourceName = s.getFirstName() + " " + s.getLastName();
			var evalResult = cedarEvaluator.evaluate(principal, "ViewEmployee", "Employee", resourceName, "Item");
			authorizationMap.put(s.getId(), evalResult.responseBody());
			cedarResourceMap.put(s.getId(), "ChildrenClinic::Employee::\"" + resourceName + "\"");
			return evalResult.isGranted();
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingAssistants.size() == 1) {
			return "redirect:/administrative-assistants/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<AdministrativeAssistant> pageContent = start > authorized.size() ? List.of()
				: authorized.subList(start, end);
		Page<AdministrativeAssistant> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listAdministrativeAssistants", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", "ChildrenClinic::Action::\"ViewEmployee\"");
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		return "administrative-assistants/administrativeAssistantsList";
	}

	@GetMapping("/administrative-assistants/{assistantId}")
	@CedarAuthorization(action = "ViewEmployee", resourceType = "Employee", validate = true)
	public ModelAndView showAdministrativeAssistant(@PathVariable("assistantId") int assistantId) {
		ModelAndView mav = new ModelAndView("administrative-assistants/administrativeAssistantDetails");
		AdministrativeAssistant administrativeAssistant = this.administrativeAssistants.findById(assistantId)
			.orElseThrow(() -> new IllegalArgumentException(
					"Administrative Assistant not found for identifier: " + assistantId));
		mav.addObject("administrativeAssistant", administrativeAssistant);
		return mav;
	}

	@GetMapping("/administrative-assistants/new")
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
				exceptionBody
					.append("You do not have permission to add administrative assistants to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		model.addAttribute("administrativeAssistant", new AdministrativeAssistant());
		return VIEWS_ADMINISTRATIVE_ASSISTANT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/administrative-assistants/new")
	public String processCreationForm(@Valid AdministrativeAssistant administrativeAssistant, BindingResult result,
			RedirectAttributes redirectAttributes, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		// Evaluate Cedar for all the submitted Clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		Collection<Clinic> submittedClinics = administrativeAssistant.getClinics();

		if (submittedClinics == null || submittedClinics.isEmpty()) {
			isAuthorized = false;
			denialReasons
				.add("You must assign the Administrative Assistant to at least one valid Clinic.");
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
				exceptionBody.append(
						"You do not have permission to add administrative assistants to one or more of the selected clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		if (StringUtils.hasLength(administrativeAssistant.getLastName())
				&& StringUtils.hasLength(administrativeAssistant.getFirstName())
				&& administrativeAssistant.isNew()) {
			boolean duplicateExists = administrativeAssistants
				.findByLastNameStartingWith(administrativeAssistant.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(s -> s.getFirstName().equalsIgnoreCase(administrativeAssistant.getFirstName())
						&& Objects.equals(s.getBirthDate(), administrativeAssistant.getBirthDate())
						&& Objects.equals(s.getGender(), administrativeAssistant.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"An administrative assistant with this first and last name, birth date, and gender already exists.");
			}
		}

		if (result.hasErrors()) {
			return VIEWS_ADMINISTRATIVE_ASSISTANT_CREATE_OR_UPDATE_FORM;
		}

		try {
			this.administrativeAssistants.save(administrativeAssistant);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			return VIEWS_ADMINISTRATIVE_ASSISTANT_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "New Administrative Assistant has been added.");
		return "redirect:/administrative-assistants/" + administrativeAssistant.getId();
	}

	@GetMapping("/administrative-assistants/{assistantId}/edit")
	@CedarAuthorization(action = "EditEmployee", resourceType = "Employee", validate = true)
	public String initUpdateForm(@PathVariable("assistantId") int assistantId, Model model) {
		AdministrativeAssistant administrativeAssistant = this.administrativeAssistants.findById(assistantId)
			.orElseThrow(
					() -> new IllegalArgumentException("Administrative Assistant not found for identifier: " + assistantId));
		model.addAttribute("administrativeAssistant", administrativeAssistant);
		return VIEWS_ADMINISTRATIVE_ASSISTANT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/administrative-assistants/{assistantId}/edit")
	public String processUpdateForm(@Valid AdministrativeAssistant administrativeAssistant, BindingResult result,
			@PathVariable("assistantId") int assistantId, RedirectAttributes redirectAttributes, HttpSession session,
			Model model) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		AdministrativeAssistant existingAssistant = this.administrativeAssistants.findById(assistantId)
			.orElseThrow(() -> new IllegalArgumentException("Administrative Assistant not found: " + assistantId));

		String resourceName = existingAssistant.getFirstName() + " " + existingAssistant.getLastName();
		var assistantEval = cedarEvaluator.evaluate(principal, "EditEmployee", "Employee", resourceName, "Page");

		if (!assistantEval.isGranted()) {
			throw new CedarDeniedException(
					"Access Denied: You do not have permission to edit this administrative assistant.\n"
							+ (assistantEval.responseBody() != null ? assistantEval.responseBody() : ""));
		}

		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();
		Collection<Clinic> submittedClinics = administrativeAssistant.getClinics();

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

		if (existingAssistant.getClinics() != null) {
			for (Clinic existingClinic : existingAssistant.getClinics()) {
				if (existingClinic == null || existingClinic.getClinicName() == null)
					continue;
				String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Background");

				if (!viewEval.isGranted()) {
					finalClinics.add(existingClinic);
				}
			}
		}
		administrativeAssistant.setClinics(finalClinics);

		if (StringUtils.hasLength(administrativeAssistant.getLastName())
				&& StringUtils.hasLength(administrativeAssistant.getFirstName())) {
			boolean duplicateExists = this.administrativeAssistants
				.findByLastNameStartingWith(administrativeAssistant.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(s -> s.getFirstName().equalsIgnoreCase(administrativeAssistant.getFirstName())
						&& Objects.equals(s.getBirthDate(), administrativeAssistant.getBirthDate())
						&& Objects.equals(s.getGender(), administrativeAssistant.getGender())
						&& !Objects.equals(s.getId(), assistantId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"An administrative assistant with this first and last name, birth date, and gender already exists.");
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("error", "There was an error in updating the administrative assistant.");
			return VIEWS_ADMINISTRATIVE_ASSISTANT_CREATE_OR_UPDATE_FORM;
		}

		administrativeAssistant.setId(assistantId);

		try {
			this.administrativeAssistants.save(administrativeAssistant);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("error", "There was an error in updating the administrative assistant.");
			return VIEWS_ADMINISTRATIVE_ASSISTANT_CREATE_OR_UPDATE_FORM;
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "Administrative Assistant values updated.");
		return "redirect:/administrative-assistants/{assistantId}";
	}

}
