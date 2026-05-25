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

package uq.pac.childrenclinic.doctor;

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

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class DoctorController {

	private static final String VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM = "doctors/createOrUpdateDoctorForm";

	private static final String DOCTOR_ROLE_NAME = "Doctor";

	private static final String DEFAULT_LEVEL_NAME = "Intern";

	private static final List<String> LEVEL_HIERARCHY = List.of("Intern", "Resident", "Staff", "Senior", "Registrar",
			"Specialist");

	private static final Set<String> NON_MANAGER_LEVELS = Set.of("Intern", "Registrar");

	private final DoctorRepository doctors;

	private final GenderRepository genders;

	private final SpecialtyRepository specialties;

	private final ClinicRepository clinics;

	private final LevelRepository levelRepository;

	private final UserRepository userRepository;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	private final ApplicationEventPublisher eventPublisher;

	private final JdbcTemplate jdbcTemplate;

	public DoctorController(DoctorRepository doctors, GenderRepository genders, SpecialtyRepository specialties,
			ClinicRepository clinics, LevelRepository levelRepository, UserRepository userRepository,
			CedarProgrammaticEvaluator cedarEvaluator, ApplicationEventPublisher eventPublisher,
			JdbcTemplate jdbcTemplate) {
		this.doctors = doctors;
		this.genders = genders;
		this.specialties = specialties;
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

	@ModelAttribute("specialties")
	public Collection<Specialty> populateSpecialties() {
		return this.specialties.findAll();
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

	@InitBinder("doctor")
	public void initDoctorBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/doctors")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String showDoctorsList(@RequestParam(defaultValue = "1") int page, Doctor doctor, BindingResult result,
			Model model, HttpSession session) {
		String lastName = doctor.getLastName() == null ? "" : doctor.getLastName();

		List<Doctor> allMatchingDoctors = this.doctors.findByLastNameStartingWith(lastName, Pageable.unpaged())
			.getContent();

		if (allMatchingDoctors.isEmpty()) {
			result.rejectValue("lastName", "notFound", "No doctors found.");
			return "redirect:/?error=noDoctors&query=" + UriUtils.encode(lastName, StandardCharsets.UTF_8);
		}

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		Map<Integer, String> authorizationMap = new HashMap<>();
		Map<Integer, String> cedarResourceMap = new HashMap<>();

		List<Doctor> authorized = allMatchingDoctors.stream().filter(d -> {
			String resourceName = d.getFirstName() + " " + d.getLastName();
			var evalResult = cedarEvaluator.evaluate(principal, "ViewEmployee", "Employee", resourceName, "Item");
			authorizationMap.put(d.getId(), evalResult.responseBody());
			cedarResourceMap.put(d.getId(), "ChildrenClinic::Employee::\"" + resourceName + "\"");
			return evalResult.isGranted();
		}).collect(Collectors.toList());

		if (authorized.size() == 1 && allMatchingDoctors.size() == 1) {
			return "redirect:/doctors/" + authorized.iterator().next().getId();
		}

		Pageable pageable = PageRequest.of(page - 1, 5);
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), authorized.size());
		List<Doctor> pageContent = start > authorized.size() ? List.of() : authorized.subList(start, end);
		Page<Doctor> paginated = new PageImpl<>(pageContent, pageable, authorized.size());

		model.addAttribute("listDoctors", paginated.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("authorizationMap", authorizationMap);
		model.addAttribute("cedarPrincipal", principal.toString());
		model.addAttribute("cedarAction", "ChildrenClinic::Action::\"ViewEmployee\"");
		model.addAttribute("cedarResourceMap", cedarResourceMap);

		return "doctors/doctorsList";
	}

	@GetMapping("/doctors/{doctorId}")
	@CedarAuthorization(action = "ViewEmployee", resourceType = "Employee", validate = true)
	public ModelAndView showDoctor(@PathVariable("doctorId") int doctorId, HttpSession session) {
		ModelAndView mav = new ModelAndView("doctors/doctorDetails");
		Doctor doctor = this.doctors.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found for identifier: " + doctorId));
		mav.addObject("doctor", doctor);

		EntityUID principal = cedarEvaluator.resolvePrincipal(session);
		String resourceName = doctor.getFirstName() + " " + doctor.getLastName();
		var editEval = cedarEvaluator.evaluate(principal, "EditEmployee", "Employee", resourceName, "Background");
		mav.addObject("canEdit", editEval.isGranted());

		return mav;
	}

	@GetMapping("/doctors/new")
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
				exceptionBody.append("You do not have permission to add doctors to any assigned clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		model.addAttribute("doctor", new Doctor());
		model.addAttribute("levels",
				levelRepository.findLevels()
					.stream()
					.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
					.collect(Collectors.toList()));
		model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
		model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));
		model.addAttribute("selectedLevelId",
				levelRepository.findByName(DEFAULT_LEVEL_NAME).map(Level::getId).orElse(null));
		model.addAttribute("selectedManagerId", null);

		return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/doctors/new")
	public String processCreationForm(@Valid Doctor doctor, BindingResult result,
			@RequestParam(name = "levelId", required = false) Integer levelId,
			@RequestParam(name = "managerId", required = false) Integer managerId,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		// Check binding/validation errors.
		if (result.hasErrors()) {
			model.addAttribute("levels",
					levelRepository.findLevels()
						.stream()
						.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
						.collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		// Check for empty clinics.
		Collection<Clinic> submittedClinics = doctor.getClinics();
		if (submittedClinics == null || submittedClinics.isEmpty()) {
			result.reject("clinicsRequired", "You must assign the Doctor to at least one valid Clinic.");
			model.addAttribute("levels",
					levelRepository.findLevels()
						.stream()
						.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
						.collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);
			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
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
				exceptionBody
					.append("You do not have permission to add doctors to one or more of the selected clinics.");
			}

			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		// Duplicate check.
		if (StringUtils.hasLength(doctor.getLastName()) && StringUtils.hasLength(doctor.getFirstName())
				&& doctor.isNew()) {
			boolean duplicateExists = doctors.findByLastNameStartingWith(doctor.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(d -> d.getFirstName().equalsIgnoreCase(doctor.getFirstName())
						&& Objects.equals(d.getBirthDate(), doctor.getBirthDate())
						&& Objects.equals(d.getGender(), doctor.getGender()));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A doctor with this first and last name, birth date, and gender already exists.");
			}
		}

		// Validate manager-level constraints.
		if (levelId != null) {
			String levelName = jdbcTemplate.queryForObject("SELECT name FROM levels WHERE id = ?", String.class,
					levelId);

			boolean requiresManager = "Intern".equals(levelName) || "Registrar".equals(levelName);

			if (requiresManager && managerId == null) {
				result.reject("managerRequired", "An Intern or Registrar Doctor must have a manager.");
			}
			else if (managerId != null) {
				Integer managerLevelId = jdbcTemplate.query(
						"SELECT level_id FROM user_role_levels WHERE user_id = ? AND role_id = "
								+ "(SELECT id FROM roles WHERE name = ?)",
						rs -> rs.next() ? rs.getInt("level_id") : null, managerId, DOCTOR_ROLE_NAME);

				String managerLevelName = null;
				if (managerLevelId != null) {
					managerLevelName = jdbcTemplate.queryForObject("SELECT name FROM levels WHERE id = ?", String.class,
							managerLevelId);
				}

				if (!isValidManager(levelName, managerLevelName)) {
					result.reject("invalidManager",
							"The selected manager's level is not high enough for a " + levelName + " Doctor.");
				}
			}
		}

		// Validate specialties constraints.
		if (levelId != null) {
			String levelName = jdbcTemplate.queryForObject("SELECT name FROM levels WHERE id = ?", String.class,
					levelId);

			Collection<Specialty> submittedSpecialties = doctor.getSpecialties();
			boolean hasSpecialties = submittedSpecialties != null && !submittedSpecialties.isEmpty();

			// Only a Specialist may have specialties.
			if (hasSpecialties && !"Specialist".equals(levelName)) {
				result.reject("invalidLevel", "Only a Specialist Doctor may have specialties.");
			}

			// A Specialist must have at least one specialty.
			if ("Specialist".equals(levelName) && !hasSpecialties) {
				result.reject("specialtiesRequired", "A Specialist Doctor must have at least one specialty.");
			}
		}

		// Final error check.
		if (result.hasErrors()) {
			model.addAttribute("levels",
					levelRepository.findLevels()
						.stream()
						.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
						.collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		try {
			this.doctors.save(doctor);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");

			model.addAttribute("levels",
					levelRepository.findLevels()
						.stream()
						.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
						.collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		createUserForDoctor(doctor, levelId, managerId);

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "New Doctor has been added.");
		return "redirect:/doctors/" + doctor.getId();
	}

	@GetMapping("/doctors/{doctorId}/edit")
	@CedarAuthorization(action = "EditEmployee", resourceType = "Employee", validate = true)
	public String initUpdateForm(@PathVariable("doctorId") int doctorId, Model model) {
		Doctor doctor = this.doctors.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found for identifier: " + doctorId));

		model.addAttribute("doctor", doctor);
		model.addAttribute("levels",
				levelRepository.findLevels()
					.stream()
					.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
					.collect(Collectors.toList()));
		model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
		model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));

		Integer currentLevelId = jdbcTemplate.query("SELECT level_id FROM user_role_levels WHERE user_id = ?",
				rs -> rs.next() ? rs.getInt("level_id") : null, doctorId);
		Integer currentManagerId = jdbcTemplate.query("SELECT manager_id FROM user_manager WHERE user_id = ?",
				rs -> rs.next() ? rs.getInt("manager_id") : null, doctorId);

		model.addAttribute("selectedLevelId", currentLevelId);
		model.addAttribute("selectedManagerId", currentManagerId);

		return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/doctors/{doctorId}/edit")
	public String processUpdateForm(@Valid Doctor doctor, BindingResult result, @PathVariable("doctorId") int doctorId,
			@RequestParam(name = "levelId", required = false) Integer levelId,
			@RequestParam(name = "managerId", required = false) Integer managerId,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		// Check binding/validation errors.
		if (result.hasErrors()) {
			model.addAttribute("error", "There was an error in updating the doctor.");
			model.addAttribute("levels",
					levelRepository.findLevels()
						.stream()
						.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
						.collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		// Cedar authorization: can the principal edit this specific doctor?
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		Doctor existingDoctor = this.doctors.findById(doctorId)
			.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

		String resourceName = existingDoctor.getFirstName() + " " + existingDoctor.getLastName();
		var doctorEval = cedarEvaluator.evaluate(principal, "EditEmployee", "Employee", resourceName, "Page");

		if (!doctorEval.isGranted()) {
			throw new CedarDeniedException("Access Denied: You do not have permission to edit this doctor.\n"
					+ (doctorEval.responseBody() != null ? doctorEval.responseBody() : ""));
		}

		// Cedar authorization on submitted clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();
		Collection<Clinic> submittedClinics = doctor.getClinics();

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

		if (existingDoctor.getClinics() != null) {
			for (Clinic existingClinic : existingDoctor.getClinics()) {
				if (existingClinic == null || existingClinic.getClinicName() == null)
					continue;
				String cedarClinicId = existingClinic.getClinicName().replaceFirst("^Clinic\\s+", "");
				var viewEval = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Background");

				if (!viewEval.isGranted()) {
					finalClinics.add(existingClinic);
				}
			}
		}
		doctor.setClinics(finalClinics);

		// Duplicate check.
		if (StringUtils.hasLength(doctor.getLastName()) && StringUtils.hasLength(doctor.getFirstName())) {
			boolean duplicateExists = this.doctors
				.findByLastNameStartingWith(doctor.getLastName(), PageRequest.of(0, 50))
				.getContent()
				.stream()
				.anyMatch(d -> d.getFirstName().equalsIgnoreCase(doctor.getFirstName())
						&& Objects.equals(d.getBirthDate(), doctor.getBirthDate())
						&& Objects.equals(d.getGender(), doctor.getGender()) && !Objects.equals(d.getId(), doctorId));

			if (duplicateExists) {
				result.rejectValue("firstName", "duplicate",
						"A doctor with this first and last name, birth date, and gender already exists.");
			}
		}

		// Validate manager-level constraints.
		if (levelId != null) {
			String levelName = jdbcTemplate.queryForObject("SELECT name FROM levels WHERE id = ?", String.class,
					levelId);

			boolean requiresManager = "Intern".equals(levelName) || "Registrar".equals(levelName);

			if (requiresManager && managerId == null) {
				result.reject("managerRequired", "An Intern or Registrar Doctor must have a manager.");
			}
			else if (managerId != null) {
				Integer managerLevelId = jdbcTemplate.query(
						"SELECT level_id FROM user_role_levels WHERE user_id = ? AND role_id = "
								+ "(SELECT id FROM roles WHERE name = ?)",
						rs -> rs.next() ? rs.getInt("level_id") : null, managerId, DOCTOR_ROLE_NAME);

				String managerLevelName = null;
				if (managerLevelId != null) {
					managerLevelName = jdbcTemplate.queryForObject("SELECT name FROM levels WHERE id = ?", String.class,
							managerLevelId);
				}

				if (!isValidManager(levelName, managerLevelName)) {
					result.reject("invalidManager",
							"The selected manager's level is not high enough for a " + levelName + " Doctor.");
				}
			}
		}

		// Validate specialties constraints.
		if (levelId != null) {
			String levelName = jdbcTemplate.queryForObject("SELECT name FROM levels WHERE id = ?", String.class,
					levelId);

			Collection<Specialty> submittedSpecialties = doctor.getSpecialties();
			boolean hasSpecialties = submittedSpecialties != null && !submittedSpecialties.isEmpty();

			// Only a Specialist may have specialties.
			if (hasSpecialties && !"Specialist".equals(levelName)) {
				result.reject("invalidLevel", "Only a Specialist Doctor may have specialties.");
			}

			// A Specialist must have at least one specialty.
			if ("Specialist".equals(levelName) && !hasSpecialties) {
				result.reject("specialtiesRequired", "A Specialist Doctor must have at least one specialty.");
			}
		}

		// Final error check.
		if (result.hasErrors()) {
			model.addAttribute("error", "There was an error in updating the doctor.");
			model.addAttribute("levels",
					levelRepository.findLevels()
						.stream()
						.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
						.collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		doctor.setId(doctorId);

		try {
			this.doctors.save(doctor);
		}
		catch (DataIntegrityViolationException ex) {
			result.rejectValue("firstName", "duplicate",
					"A person with this first name, last name, birth date, and gender already exists.");
			model.addAttribute("error", "There was an error in updating the doctor.");
			model.addAttribute("levels",
					levelRepository.findLevels()
						.stream()
						.filter(l -> LEVEL_HIERARCHY.contains(l.getName()))
						.collect(Collectors.toList()));
			model.addAttribute("potentialManagers", userRepository.findByRoleName(DOCTOR_ROLE_NAME));
			model.addAttribute("managerLevelMap", buildManagerLevelMap(DOCTOR_ROLE_NAME));
			model.addAttribute("selectedLevelId", levelId);
			model.addAttribute("selectedManagerId", managerId);

			return VIEWS_DOCTOR_CREATE_OR_UPDATE_FORM;
		}

		String updatedUsername = doctor.getFirstName() + " " + doctor.getLastName();
		jdbcTemplate.update("UPDATE users SET username = ? WHERE entity_id = ?", updatedUsername, doctorId);

		Integer roleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = ?", Integer.class,
				DOCTOR_ROLE_NAME);
		jdbcTemplate.update("DELETE FROM user_role_levels WHERE user_id = ?", doctorId);
		if (levelId != null) {
			jdbcTemplate.update("INSERT INTO user_role_levels (user_id, role_id, level_id) VALUES (?, ?, ?)", doctorId,
					roleId, levelId);
		}
		jdbcTemplate.update("DELETE FROM user_manager WHERE user_id = ?", doctorId);
		if (managerId != null) {
			jdbcTemplate.update("INSERT INTO user_manager (user_id, manager_id) VALUES (?, ?)", doctorId, managerId);
		}

		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "Doctor values updated.");
		return "redirect:/doctors/{doctorId}";
	}

	private void createUserForDoctor(Doctor doctor, Integer levelId, Integer managerId) {
		Integer entityId = doctor.getId();
		String username = doctor.getFirstName() + " " + doctor.getLastName();

		jdbcTemplate.update("INSERT INTO users (entity_id, username) VALUES (?, ?)", entityId, username);

		Integer roleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = ?", Integer.class,
				DOCTOR_ROLE_NAME);
		jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", entityId, roleId);

		if (levelId != null) {
			jdbcTemplate.update("INSERT INTO user_role_levels (user_id, role_id, level_id) VALUES (?, ?, ?)", entityId,
					roleId, levelId);
		}
		else {
			Integer defaultLevelId = jdbcTemplate.queryForObject("SELECT id FROM levels WHERE name = ?", Integer.class,
					DEFAULT_LEVEL_NAME);
			jdbcTemplate.update("INSERT INTO user_role_levels (user_id, role_id, level_id) VALUES (?, ?, ?)", entityId,
					roleId, defaultLevelId);
		}

		if (managerId != null) {
			jdbcTemplate.update("INSERT INTO user_manager (user_id, manager_id) VALUES (?, ?)", entityId, managerId);
		}
	}

	private Map<Integer, String> buildManagerLevelMap(String roleName) {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT url.user_id, l.name AS level_name "
				+ "FROM user_role_levels url " + "JOIN levels l ON url.level_id = l.id "
				+ "JOIN roles r ON url.role_id = r.id " + "WHERE r.name = ?", roleName);

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
