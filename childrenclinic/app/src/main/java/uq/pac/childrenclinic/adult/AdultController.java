package uq.pac.childrenclinic.adult;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarRequest;
import uq.pac.childrenclinic.cedar.CedarService;

@Controller
public class AdultController {

	private static final String VIEWS_ADULT_CREATE_OR_UPDATE_FORM = "adults/createOrUpdateAdultForm";

	private final AdultRepository adults;

	private final CedarService cedarService;

	public AdultController(AdultRepository adults, CedarService cedarService) {
		this.adults = adults;
		this.cedarService = cedarService;
	}

	@InitBinder("adult")
	public void initAdultBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/adults/find")
	@CedarAuthorization(action = "ListAdults", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initFindForm(Model model) {
		model.addAttribute("adult", new Adult());
		return "adults/findAdults";
	}

	@GetMapping("/adults")
	@CedarAuthorization(action = "ListAdults", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Adult adult, BindingResult result,
			Model model, HttpSession session) {
		String lastName = adult.getLastName() == null ? "" : adult.getLastName();

		List<Adult> allMatchingAdults = this.adults.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingAdults.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No adults found.");
			return "adults/findAdults";
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

		EntityUID action = EntityUID.parse("ChildrenClinic::Action::\"ViewAdult\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));

		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Adult> authorized = allMatchingAdults.stream().filter(a -> {
			String resourceName = a.getFirstName() + " " + a.getLastName();
			EntityUID resource = EntityUID.parse("ChildrenClinic::ResponsibleAdult::\"" + resourceName + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

			String access = cedarService
				.checkAccess(new CedarRequest(principal, action, resource, new HashMap<>(), true))
				.getBody();
			authorizationMap.put(a.getId(), access);
			cedarResourceMap.put(a.getId(), "ChildrenClinic::ResponsibleAdult::\"" + resourceName + "\"");
			return access != null && access.startsWith("Access Granted.");
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingAdults.size() == 1) {
			return "redirect:/adults/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Adult> pageContent = start > authorized.size() ? List.of() : authorized.subList(start, end);
		Page<Adult> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listAdults", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", action.toString());
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		return "adults/adultsList";
	}

	/**
	 * Retrieves and renders the details of a specific Adult entity.
	 */
	@GetMapping("/adults/{adultId}")
	@CedarAuthorization(action = "ViewAdult", resourceType = "ResponsibleAdult", validate = true)
	public ModelAndView showAdult(@PathVariable("adultId") int adultId) {
		ModelAndView mav = new ModelAndView("adults/adultDetails");
		Adult adult = this.adults.findById(adultId)
			.orElseThrow(() -> new IllegalArgumentException("Adult not found with identifier: " + adultId));
		mav.addObject("adult", adult);
		return mav;
	}

	@GetMapping("/adults/new")
	@CedarAuthorization(action = "AddAdult", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initCreationForm(Model model) {
		model.addAttribute("adult", new Adult());
		return VIEWS_ADULT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/adults/new")
	@CedarAuthorization(action = "AddAdult", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processCreationForm(@Valid Adult adult, BindingResult result, RedirectAttributes redirectAttributes) {
		if (StringUtils.hasLength(adult.getLastName()) && StringUtils.hasLength(adult.getFirstName())
				&& adult.isNew()) {
			boolean duplicateExists = adults.findByLastNameStartingWith(adult.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(p -> p.getFirstName().equalsIgnoreCase(adult.getFirstName()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "An adult with this first and last name already exists");
			}
		}
		if (result.hasErrors())
			return VIEWS_ADULT_CREATE_OR_UPDATE_FORM;
		this.adults.save(adult);
		redirectAttributes.addFlashAttribute("message", "New Adult Added.");
		return "redirect:/adults/find";
	}

	/**
	 * Initializes the form for updating an existing Adult entity.
	 */
	@GetMapping("/adults/{adultId}/edit")
	@CedarAuthorization(action = "EditAdult", resourceType = "ResponsibleAdult", validate = true)
	public String initUpdateForm(@PathVariable("adultId") int adultId, Model model) {
		Adult adult = this.adults.findById(adultId)
			.orElseThrow(() -> new IllegalArgumentException("Adult not found with identifier: " + adultId));
		model.addAttribute("adult", adult);
		return VIEWS_ADULT_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes the submission of the Adult update form.
	 */
	@PostMapping("/adults/{adultId}/edit")
	@CedarAuthorization(action = "EditAdult", resourceType = "ResponsibleAdult", validate = true)
	public String processUpdateForm(@Valid Adult adult, BindingResult result, @PathVariable("adultId") int adultId,
			RedirectAttributes redirectAttributes) {
		if (StringUtils.hasLength(adult.getLastName()) && StringUtils.hasLength(adult.getFirstName())) {
			boolean duplicateExists = this.adults.findByLastNameStartingWith(adult.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(a -> a.getFirstName().equalsIgnoreCase(adult.getFirstName())
						&& !Objects.equals(a.getId(), adultId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "An adult with this first and last name already exists.");
			}
		}
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the adult.");
			return VIEWS_ADULT_CREATE_OR_UPDATE_FORM;
		}

		adult.setId(adultId);
		this.adults.save(adult);

		redirectAttributes.addFlashAttribute("message", "Adult Values Updated.");
		return "redirect:/adults/{adultId}";
	}

}
