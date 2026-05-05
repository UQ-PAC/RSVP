package uq.pac.childrenclinic.adult;

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
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarDeniedException;
import uq.pac.childrenclinic.cedar.CedarProgrammaticEvaluator;
import uq.pac.childrenclinic.model.Gender;
import uq.pac.childrenclinic.model.GenderRepository;
import uq.pac.childrenclinic.system.Clinic;
import uq.pac.childrenclinic.system.ClinicRepository;

@Controller
public class AdultController {

	private static final String VIEWS_ADULT_CREATE_OR_UPDATE_FORM = "adults/createOrUpdateAdultForm";

	private final AdultRepository adults;

	private final GenderRepository genders;

	private final ClinicRepository clinics;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	public AdultController(AdultRepository adults, GenderRepository genders, ClinicRepository clinics, CedarProgrammaticEvaluator cedarEvaluator) {
		this.adults = adults;
		this.genders = genders;
		this.clinics = clinics;
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

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Adult> authorized = allMatchingAdults.stream().filter(a -> {
			String resourceName = a.getFirstName() + " " + a.getLastName();
			var evalResult = cedarEvaluator.evaluate(principal, "ViewAdult", "ResponsibleAdult", resourceName, "Item");
			authorizationMap.put(a.getId(), evalResult.responseBody());
			cedarResourceMap.put(a.getId(), "ChildrenClinic::ResponsibleAdult::\"" + resourceName + "\"");
			return evalResult.isGranted();
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
		model.addAttribute("cedarAction", "ChildrenClinic::Action::\"ViewAdult\"");
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
	public String initCreationForm(Model model, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Collection<Clinic> allClinics = this.clinics.findClinics();

		// Evaluate Cedar for each Clinic and log the result.
		boolean isAuthorized = false;
		List<String> denialReasons = new ArrayList<>();

		for (Clinic clinic : allClinics) {
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var result = cedarEvaluator.evaluate(principal, "AddAdult", "Clinic", cedarClinicId, "Page");

			if (result.isGranted()) {
				isAuthorized = true;
			} else if (result.responseBody() != null) {
				denialReasons.add(result.responseBody());
			}
		}

		// Deny Access if no clinics passed the check.
		if (!isAuthorized) {
			String prefix = "Access Denied.\n";
			StringBuilder exceptionBody = new StringBuilder("Access Denied by the Cedar Policy Engine.\n\n");

			if (!denialReasons.isEmpty()) {
				// Combine all denial reasons and strip out the redundant prefix from each.
				for (String reason : denialReasons) {
					exceptionBody.append(reason.replaceAll("(?m)^" + prefix, "")).append("\n");
				}
			} else {
				exceptionBody.append("You do not have permission to add adults to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}


		model.addAttribute("adult", new Adult());
		return VIEWS_ADULT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/adults/new")
	public String processCreationForm(@Valid Adult adult, BindingResult result, RedirectAttributes redirectAttributes, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		// Evaluate Cedar for all the submitted Clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		Collection<Clinic> submittedClinics = adult.getClinics();

		if (submittedClinics == null || submittedClinics.isEmpty()) {
			isAuthorized = false;
			denialReasons.add("You must assign the Adult to at least one valid Clinic.");
		} else {
			for (Clinic clinic : submittedClinics) {
				String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var evalResult = cedarEvaluator.evaluate(principal, "AddAdult", "Clinic", cedarClinicId, "Page");

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
			} else {
				exceptionBody.append("You do not have permission to add adults to one or more of the selected clinics.");
			}
			
			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		if (StringUtils.hasLength(adult.getLastName()) && StringUtils.hasLength(adult.getFirstName())
				&& adult.isNew()) {
			boolean duplicateExists = adults.findByLastNameStartingWith(adult.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(a -> a.getFirstName().equalsIgnoreCase(adult.getFirstName()) &&
						  a.getBirthDate().equals(adult.getBirthDate()) &&
						  a.getGender().equals(adult.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "An adult with this first and last name, birth date, and gender already exists.");
			}
		}

		if (result.hasErrors()){
			return VIEWS_ADULT_CREATE_OR_UPDATE_FORM;
		}

		this.adults.save(adult);
		redirectAttributes.addFlashAttribute("message", "New Adult has been added.");
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
	public String processUpdateForm(@Valid Adult adult, BindingResult result, @PathVariable("adultId") int adultId,
			RedirectAttributes redirectAttributes, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Adult existingAdult = this.adults.findById(adultId)
			.orElseThrow(() -> new IllegalArgumentException("Adult not found: " + adultId));

		String resourceName = existingAdult.getFirstName() + " " + existingAdult.getLastName();
		var adultEval = cedarEvaluator.evaluate(principal, "EditAdult", "Adult", resourceName, "Page");

		if (!adultEval.isGranted()) {
			throw new CedarDeniedException("Access Denied: You do not have permission to edit this adult.\n" 
					+ (adultEval.responseBody() != null ? adultEval.responseBody() : ""));
		}

		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();
		Collection<Clinic> submittedClinics = adult.getClinics();

		if (submittedClinics != null) {
			for (Clinic clinic : submittedClinics) {
				String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var clinicEval = cedarEvaluator.evaluate(principal, "EditAdult", "Clinic", cedarClinicId, "Page");
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

		if (StringUtils.hasLength(adult.getLastName()) && StringUtils.hasLength(adult.getFirstName())) {
			boolean duplicateExists = this.adults.findByLastNameStartingWith(adult.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(a -> a.getFirstName().equalsIgnoreCase(adult.getFirstName()) &&
						  a.getBirthDate().equals(adult.getBirthDate()) &&
						  a.getGender().equals(adult.getGender()) &&
						  !Objects.equals(a.getId(), adultId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate", "An adult with this first and last name, birth date, and gender already exists.");
			}
		}

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the adult.");
			return VIEWS_ADULT_CREATE_OR_UPDATE_FORM;
		}

		Set<Clinic> finalClinics = new HashSet<>(submittedClinics != null ? submittedClinics : new ArrayList<>());

		for (Clinic existingClinic : existingAdult.getClinics()) {
			String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Background");
			
			// If the user did NOT have permission to view this clinic, it means it wasn't in the form. 
			// We must re-add it to the final payload to prevent it from being deleted.
			if (!viewEval.isGranted()) {
				finalClinics.add(existingClinic);
			}
		}
		adult.setClinics(finalClinics);

		adult.setId(adultId);
		this.adults.save(adult);
		redirectAttributes.addFlashAttribute("message", "Adult values updated.");
		return "redirect:/adults/{adultId}";
	}

}
