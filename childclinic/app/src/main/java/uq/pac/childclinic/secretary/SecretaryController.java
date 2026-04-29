package uq.pac.childclinic.secretary;

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

@Controller
public class SecretaryController {

	private static final String VIEWS_SECRETARY_CREATE_OR_UPDATE_FORM = "secretaries/createOrUpdateSecretaryForm";

	private final SecretaryRepository secretaries;

	private final ClinicRepository clinics;

	private final CedarService cedarService;

	public SecretaryController(SecretaryRepository secretaries, ClinicRepository clinics, CedarService cedarService) {
		this.secretaries = secretaries;
		this.clinics = clinics;
		this.cedarService = cedarService;
	}

	@ModelAttribute("clinics")
	public Collection<Clinic> populateClinics() {
		return this.clinics.findClinics();
	}

	@InitBinder("secretary")
	public void initSecretaryBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/secretaries/find")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initFindForm(Model model) {
		model.addAttribute("secretary", new Secretary());
		return "secretaries/findSecretaries";
	}

	@GetMapping("/secretaries")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Secretary secretary, BindingResult result,
			Model model, HttpSession session) {
		String lastName = secretary.getLastName() == null ? "" : secretary.getLastName();

		List<Secretary> allMatchingSecretaries = this.secretaries
			.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingSecretaries.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No secretaries found.");
			return "secretaries/findSecretaries";
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

		List<Secretary> authorized = allMatchingSecretaries.stream().filter(s -> {
			String resourceName = s.getFirstName() + " " + s.getLastName();
			EntityUID resource = EntityUID.parse("ChildrenClinic::Employee::\"" + resourceName + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

			String access = cedarService
				.checkAccess(new CedarRequest(principal, action, resource, new HashMap<>(), true))
				.getBody();
			authorizationMap.put(s.getId(), access);
			cedarResourceMap.put(s.getId(), "ChildrenClinic::Employee::\"" + resourceName + "\"");
			return access != null && access.startsWith("Access Granted.");
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingSecretaries.size() == 1) {
			return "redirect:/secretaries/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Secretary> pageContent = start > authorized.size() ? List.of()
				: authorized.subList(start, end);
		Page<Secretary> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listSecretaries", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", action.toString());
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		return "secretaries/secretariesList";
	}

	@GetMapping("/secretaries/{secretaryId}")
	@CedarAuthorization(action = "ViewEmployee", resourceType = "Employee", validate = true)
	public ModelAndView showSecretary(@PathVariable("secretaryId") int secretaryId) {
		ModelAndView mav = new ModelAndView("secretaries/secretaryDetails");
		Secretary secretary = this.secretaries.findById(secretaryId)
			.orElseThrow(() -> new IllegalArgumentException("Secretary not found for identifier: " + secretaryId));
		mav.addObject("secretary", secretary);
		return mav;
	}

	@GetMapping("/secretaries/new")
	@CedarAuthorization(action = "AddEmployee", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initCreationForm(Model model) {
		model.addAttribute("secretary", new Secretary());
		return VIEWS_SECRETARY_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/secretaries/new")
	@CedarAuthorization(action = "AddEmployee", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processCreationForm(@Valid Secretary secretary, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.hasLength(secretary.getLastName()) && StringUtils.hasLength(secretary.getFirstName())
				&& secretary.isNew()) {
			boolean duplicateExists = secretaries
				.findByLastNameStartingWith(secretary.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(s -> s.getFirstName().equalsIgnoreCase(secretary.getFirstName()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A secretary with this first and last name already exists");
			}
		}
		if (result.hasErrors())
			return VIEWS_SECRETARY_CREATE_OR_UPDATE_FORM;
		this.secretaries.save(secretary);
		redirectAttributes.addFlashAttribute("message", "New Secretary has been added.");
		return "redirect:/secretaries/" + secretary.getId();
	}

	@GetMapping("/secretaries/{secretaryId}/edit")
	@CedarAuthorization(action = "EditEmployee", resourceType = "Employee", validate = true)
	public String initUpdateForm(@PathVariable("secretaryId") int secretaryId, Model model) {
		Secretary secretary = this.secretaries.findById(secretaryId)
			.orElseThrow(() -> new IllegalArgumentException("Secretary not found for identifier: " + secretaryId));
		model.addAttribute("secretary", secretary);
		return VIEWS_SECRETARY_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/secretaries/{secretaryId}/edit")
	@CedarAuthorization(action = "EditEmployee", resourceType = "Employee", validate = true)
	public String processUpdateForm(@Valid Secretary secretary, BindingResult result,
			@PathVariable("secretaryId") int secretaryId, RedirectAttributes redirectAttributes) {
		if (StringUtils.hasLength(secretary.getLastName()) && StringUtils.hasLength(secretary.getFirstName())) {
			boolean duplicateExists = this.secretaries
				.findByLastNameStartingWith(secretary.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(s -> s.getFirstName().equalsIgnoreCase(secretary.getFirstName())
						&& !Objects.equals(s.getId(), secretaryId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A secretary with this first and last name already exists.");
			}
		}

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the secretary.");
			return VIEWS_SECRETARY_CREATE_OR_UPDATE_FORM;
		}

		secretary.setId(secretaryId);
		this.secretaries.save(secretary);

		redirectAttributes.addFlashAttribute("message", "Secretary Values Updated.");
		return "redirect:/secretaries/{secretaryId}";
	}

}
