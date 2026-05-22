package uq.pac.childrenclinic.receptionist;

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
import org.springframework.jdbc.core.JdbcTemplate;
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
import uq.pac.childrenclinic.system.Clinic;
import uq.pac.childrenclinic.system.ClinicRepository;
import uq.pac.childrenclinic.system.Level;
import uq.pac.childrenclinic.system.LevelRepository;
import uq.pac.childrenclinic.system.UserRepository;

@Controller
public class ReceptionistController {

	private static final String VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM = "receptionists/createOrUpdateReceptionistForm";

	private static final String RECEPTIONIST_ROLE_NAME = "Receptionist";

	private static final String DEFAULT_LEVEL_NAME = "Intern";

	private static final List<String> LEVEL_HIERARCHY = List.of("Intern", "Staff", "Senior");

	private static final Set<String> NON_MANAGER_LEVELS = Set.of("Intern");

	private final ReceptionistRepository receptionists;

	private final GenderRepository genders;

	private final ClinicRepository clinics;

	private final LevelRepository levelRepository;

	private final UserRepository userRepository;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	private final ApplicationEventPublisher eventPublisher;

	private final JdbcTemplate jdbcTemplate;

	public ReceptionistController(ReceptionistRepository receptionists, GenderRepository genders,
			ClinicRepository clinics, LevelRepository levelRepository, UserRepository userRepository,
			CedarProgrammaticEvaluator cedarEvaluator, ApplicationEventPublisher eventPublisher,
			JdbcTemplate jdbcTemplate) {
		this.receptionists = receptionists;
		this.genders = genders;
		this.clinics = clinics;
		this.levelRepository = levelRepository;
		this.userRepository = userRepository;
		this.cedarEvaluator = cedarEvaluator;
		this.eventPublisher = eventPublisher;
		this.jdbcTemplate = jdbcTemplate;
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

	@InitBinder("receptionist")
	public void initReceptionistBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/receptionists")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processFindForm(@RequestParam(defaultValue = "1") int page,
			Receptionist receptionist, BindingResult result, Model model, HttpSession session) {
		String lastName = receptionist.getLastName() == null ? "" : receptionist.getLastName();

		List<Receptionist> allMatchingReceptionists = this.receptionists
			.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingReceptionists.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No receptionists found.");
			return "redirect:/?error=noReceptionists&query=" + UriUtils.encode(lastName, StandardCharsets.UTF_8);
		}

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Receptionist> authorized = allMatchingReceptionists.stream().filter(s -> {
			String resourceName = s.getFirstName() + " " + s.getLastName();
			var evalResult = cedarEvaluator.evaluate(principal, "ViewEmployee", "Employee", resourceName, "Item");
			authorizationMap.put(s.getId(), evalResult.responseBody());
			cedarResourceMap.put(s.getId(), "ChildrenClinic::Employee::\"" + resourceName + "\"");
			return evalResult.isGranted();
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingReceptionists.size() == 1) {
			return "redirect:/receptionists/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Receptionist> pageContent = start > authorized.size() ? List.of()
				: authorized.subList(start, end);
		Page<Receptionist> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listReceptionists", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", "ChildrenClinic::Action::\"ViewEmployee\"");
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		return "receptionists/receptionistsList";
	}

	@GetMapping("/receptionists/{receptionistId}")
	@CedarAuthorization(action = "ViewEmployee", resourceType = "Employee", validate = true)
	public ModelAndView showReceptionist(@PathVariable("receptionistId") int receptionistId, HttpSession session) {
		ModelAndView mav = new ModelAndView("receptionists/receptionistDetails");
		Receptionist receptionist = this.receptionists.findById(receptionistId)
			.orElseThrow(() -> new IllegalArgumentException(
					"Receptionist not found for identifier: " + receptionistId));
		mav.addObject("receptionist", receptionist);
 
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		String resourceName = receptionist.getFirstName() + " " + receptionist.getLastName();
		var editEval = cedarEvaluator.evaluate(principal, "EditEmployee", "Employee", resourceName, "Background");
		mav.addObject("canEdit", editEval.isGranted());
 
		return mav;
	}

	@GetMapping("/receptionists/new")
	public String initCreationForm(Model model, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Collection<Clinic> allClinics = this.clinics.findClinics();

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
					.append("You do not have permission to add receptionists to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		model.addAttribute("receptionist", new Receptionist());
		model.addAttribute("levels", levelRepository.findLevels().stream().filter(l -> LEVEL_HIERARCHY.contains(l.getName())).collect(Collectors.toList()));
		model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
		model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));
		model.addAttribute("selectedLevelId", levelRepository.findByName(DEFAULT_LEVEL_NAME).map(Level::getId).orElse(null));
		model.addAttribute("selectedManagerId", null);

		return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/receptionists/new")
	public String processCreationForm(@Valid Receptionist receptionist, BindingResult result,
			@RequestParam(name = "levelId", required = false) Integer levelId,
			@RequestParam(name = "managerId", required = false) Integer managerId,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		// Check binding/validation errors.
		if (result.hasErrors()) {
			model.addAttribute("levels", levelRepository.findLevels().stream().filter(l -> LEVEL_HIERARCHY.contains(l.getName())).collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
		}

		// Check for empty clinics.
		Collection<Clinic> submittedClinics = receptionist.getClinics();
		if (submittedClinics == null || submittedClinics.isEmpty()) {
			result.reject("clinicsRequired", "You must assign the Receptionist to at least one valid Clinic.");
			model.addAttribute("levels", levelRepository.findLevels());
			model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);
			return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
		}

		// Cedar authorization on submitted clinics.
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

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
						"You do not have permission to add receptionists to one or more of the selected clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		// Duplicate check.
		if (StringUtils.hasLength(receptionist.getLastName())
				&& StringUtils.hasLength(receptionist.getFirstName()) && receptionist.isNew()) {
			boolean duplicateExists = receptionists
				.findByLastNameStartingWith(receptionist.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(s -> s.getFirstName().equalsIgnoreCase(receptionist.getFirstName())
						&& Objects.equals(s.getBirthDate(), receptionist.getBirthDate())
						&& Objects.equals(s.getGender(), receptionist.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A receptionist with this first and last name, birth date, and gender already exists.");
			}
		}

		// Validate manager-level constraints.
		if (levelId != null) {
			String levelName = jdbcTemplate.queryForObject(
					"SELECT name FROM levels WHERE id = ?", String.class, levelId);

			boolean requiresManager = "Intern".equals(levelName);

			if (requiresManager && managerId == null) {
				result.reject("managerRequired",
						"An Intern Receptionist must have a manager.");
			}
			else if (managerId != null) {
				Integer managerLevelId = jdbcTemplate.query(
						"SELECT level_id FROM user_role_levels WHERE user_id = ? AND role_id = "
					+ "(SELECT id FROM roles WHERE name = ?)",
						rs -> rs.next() ? rs.getInt("level_id") : null,
						managerId, RECEPTIONIST_ROLE_NAME);

				String managerLevelName = null;
				if (managerLevelId != null) {
					managerLevelName = jdbcTemplate.queryForObject(
							"SELECT name FROM levels WHERE id = ?", String.class, managerLevelId);
				}

				if (!isValidManager(levelName, managerLevelName)) {
					result.reject("invalidManager",
							"The selected manager's level is not high enough for a "
							+ levelName + " Receptionist.");
				}
			}
		}

		// Final error check.
		if (result.hasErrors()) {
			model.addAttribute("levels", levelRepository.findLevels().stream().filter(l -> LEVEL_HIERARCHY.contains(l.getName())).collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
		}

		try {
			this.receptionists.save(receptionist);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");

			model.addAttribute("levels", levelRepository.findLevels().stream().filter(l -> LEVEL_HIERARCHY.contains(l.getName())).collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
		}

		createUserForReceptionist(receptionist, levelId, managerId);

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "New Receptionist has been added.");
		return "redirect:/receptionists/" + receptionist.getId();
	}

	@GetMapping("/receptionists/{receptionistId}/edit")
	@CedarAuthorization(action = "EditEmployee", resourceType = "Employee", validate = true)
	public String initUpdateForm(@PathVariable("receptionistId") int receptionistId, Model model) {
		Receptionist receptionist = this.receptionists.findById(receptionistId)
			.orElseThrow(() -> new IllegalArgumentException(
					"Receptionist not found for identifier: " + receptionistId));

		model.addAttribute("receptionist", receptionist);
		model.addAttribute("levels", levelRepository.findLevels().stream().filter(l -> LEVEL_HIERARCHY.contains(l.getName())).collect(Collectors.toList()));
		model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
		model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));

		Integer currentLevelId = jdbcTemplate.query(
				"SELECT level_id FROM user_role_levels WHERE user_id = ?",
				rs -> rs.next() ? rs.getInt("level_id") : null,
				receptionistId);
		Integer currentManagerId = jdbcTemplate.query(
				"SELECT manager_id FROM user_manager WHERE user_id = ?",
				rs -> rs.next() ? rs.getInt("manager_id") : null,
				receptionistId);

		model.addAttribute("selectedLevelId", currentLevelId);
		model.addAttribute("selectedManagerId", currentManagerId);

		return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/receptionists/{receptionistId}/edit")
	public String processUpdateForm(@Valid Receptionist receptionist, BindingResult result, @PathVariable("receptionistId") int receptionistId,
			@RequestParam(name = "levelId", required = false) Integer levelId,
			@RequestParam(name = "managerId", required = false) Integer managerId,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		if (result.hasErrors()) {
			model.addAttribute("error", "There was an error in updating the receptionist.");
			model.addAttribute("levels", levelRepository.findLevels().stream().filter(l -> LEVEL_HIERARCHY.contains(l.getName())).collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
		}

		// Cedar authorization: can the principal edit this specific receptionist?
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Receptionist existingReceptionist = this.receptionists.findById(receptionistId)
			.orElseThrow(() -> new IllegalArgumentException("Receptionist not found: " + receptionistId));

		String resourceName = existingReceptionist.getFirstName() + " " + existingReceptionist.getLastName();
		var receptionistEval = cedarEvaluator.evaluate(principal, "EditEmployee", "Employee", resourceName, "Page");

		if (!receptionistEval.isGranted()) {
			throw new CedarDeniedException(
					"Access Denied: You do not have permission to edit this receptionist.\n"
							+ (receptionistEval.responseBody() != null ? receptionistEval.responseBody() : ""));
		}

		// Cedar authorization on submitted clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();
		Collection<Clinic> submittedClinics = receptionist.getClinics();

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

		if (existingReceptionist.getClinics() != null) {
			for (Clinic existingClinic : existingReceptionist.getClinics()) {
				if (existingClinic == null || existingClinic.getClinicName() == null)
					continue;
				String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Background");

				if (!viewEval.isGranted()) {
					finalClinics.add(existingClinic);
				}
			}
		}
		receptionist.setClinics(finalClinics);

		// Duplicate check.
		if (StringUtils.hasLength(receptionist.getLastName())
				&& StringUtils.hasLength(receptionist.getFirstName())) {
			boolean duplicateExists = this.receptionists
				.findByLastNameStartingWith(receptionist.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(s -> s.getFirstName().equalsIgnoreCase(receptionist.getFirstName())
						&& Objects.equals(s.getBirthDate(), receptionist.getBirthDate())
						&& Objects.equals(s.getGender(), receptionist.getGender())
						&& !Objects.equals(s.getId(), receptionistId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A receptionist with this first and last name, birth date, and gender already exists.");
			}
		}

		// Validate manager-level constraints.
		if (levelId != null) {
			String levelName = jdbcTemplate.queryForObject(
					"SELECT name FROM levels WHERE id = ?", String.class, levelId);

			boolean requiresManager = "Intern".equals(levelName);

			if (requiresManager && managerId == null) {
				result.reject("managerRequired",
						"An Intern Receptionist must have a manager.");
			}
			else if (managerId != null) {
				Integer managerLevelId = jdbcTemplate.query(
						"SELECT level_id FROM user_role_levels WHERE user_id = ? AND role_id = "
					+ "(SELECT id FROM roles WHERE name = ?)",
						rs -> rs.next() ? rs.getInt("level_id") : null,
						managerId, RECEPTIONIST_ROLE_NAME);

				String managerLevelName = null;
				if (managerLevelId != null) {
					managerLevelName = jdbcTemplate.queryForObject(
							"SELECT name FROM levels WHERE id = ?", String.class, managerLevelId);
				}

				if (!isValidManager(levelName, managerLevelName)) {
					result.reject("invalidManager",
							"The selected manager's level is not high enough for a "
							+ levelName + " Receptionist.");
				}
			}
		}

		// Final error check.
		if (result.hasErrors()) {
			model.addAttribute("error", "There was an error in updating the receptionist.");
			model.addAttribute("levels", levelRepository.findLevels().stream().filter(l -> LEVEL_HIERARCHY.contains(l.getName())).collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
		}

		receptionist.setId(receptionistId);

		try {
			this.receptionists.save(receptionist);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("error", "There was an error in updating the receptionist.");
			model.addAttribute("levels", levelRepository.findLevels().stream().filter(l -> LEVEL_HIERARCHY.contains(l.getName())).collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(RECEPTIONIST_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_RECEPTIONIST_CREATE_OR_UPDATE_FORM;
		}

		String updatedUsername = receptionist.getFirstName() + " " + receptionist.getLastName();
		jdbcTemplate.update("UPDATE users SET username = ? WHERE entity_id = ?", updatedUsername, receptionistId);

		Integer roleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = ?", Integer.class,
				RECEPTIONIST_ROLE_NAME);
		jdbcTemplate.update("DELETE FROM user_role_levels WHERE user_id = ?", receptionistId);
		if (levelId != null) {
			jdbcTemplate.update("INSERT INTO user_role_levels (user_id, role_id, level_id) VALUES (?, ?, ?)",
					receptionistId, roleId, levelId);
		}
		jdbcTemplate.update("DELETE FROM user_manager WHERE user_id = ?", receptionistId);
		if (managerId != null) {
			jdbcTemplate.update("INSERT INTO user_manager (user_id, manager_id) VALUES (?, ?)",
					receptionistId, managerId);
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "Receptionist values updated.");
		return "redirect:/receptionists/{receptionistId}";
	}

	private void createUserForReceptionist(Receptionist receptionist, Integer levelId, Integer managerId) {
		Integer entityId = receptionist.getId();
		String username = receptionist.getFirstName() + " " + receptionist.getLastName();
 
		jdbcTemplate.update("INSERT INTO users (entity_id, username) VALUES (?, ?)", entityId, username);
 
		Integer roleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = ?", Integer.class,
				RECEPTIONIST_ROLE_NAME);
		jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", entityId, roleId);
 
		if (levelId != null) {
			jdbcTemplate.update("INSERT INTO user_role_levels (user_id, role_id, level_id) VALUES (?, ?, ?)",
					entityId, roleId, levelId);
		}
		else {
			Integer defaultLevelId = jdbcTemplate.queryForObject("SELECT id FROM levels WHERE name = ?",
					Integer.class, DEFAULT_LEVEL_NAME);
			jdbcTemplate.update("INSERT INTO user_role_levels (user_id, role_id, level_id) VALUES (?, ?, ?)",
					entityId, roleId, defaultLevelId);
		}

		if (managerId != null) {
			jdbcTemplate.update("INSERT INTO user_manager (user_id, manager_id) VALUES (?, ?)",
					entityId, managerId);
		}
	}

	private Map<Integer, String> buildManagerLevelMap(String roleName) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
			"SELECT url.user_id, l.name AS level_name "
			+ "FROM user_role_levels url "
			+ "JOIN levels l ON url.level_id = l.id "
			+ "JOIN roles r ON url.role_id = r.id "
			+ "WHERE r.name = ?", roleName);

		Map<Integer, String> result = new HashMap<>();
		for (Map<String, Object> row : rows) {
			result.put((Integer) row.get("user_id"), (String) row.get("level_name"));
		}
		return result;
	}

	private boolean isValidManager(String entityLevelName, String managerLevelName) {
		if (managerLevelName == null) {
			return false;
		}
		if (NON_MANAGER_LEVELS.contains(managerLevelName)) {
			return false;
		}
		int entityRank = LEVEL_HIERARCHY.indexOf(entityLevelName);
		int managerRank = LEVEL_HIERARCHY.indexOf(managerLevelName);
		return entityRank >= 0 && managerRank >= 0 && managerRank > entityRank;
	}
}
