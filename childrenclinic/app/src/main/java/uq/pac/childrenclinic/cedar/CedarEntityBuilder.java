package uq.pac.childrenclinic.cedar;

import com.cedarpolicy.model.entity.Entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// Dynamically constructs Cedar Entities from the database.
@Component
public class CedarEntityBuilder {

	private static final Logger logger = LoggerFactory.getLogger(CedarEntityBuilder.class);

	private static final String NAMESPACE = "ChildrenClinic";

	private final JdbcTemplate jdbcTemplate;

	private final ObjectMapper objectMapper;

	public CedarEntityBuilder(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = new ObjectMapper();
	}

	// Builds the complete set of Cedar entities from the current database state.
	public Entities buildEntities() {
		List<Map<String, Object>> allEntities = new ArrayList<>();

		// Static reference entities (lookup tables).
		allEntities.addAll(buildClinicEntities());
		allEntities.addAll(buildGuardianAuthorityEntities());
		allEntities.addAll(buildConfidentialityEntities());
		allEntities.addAll(buildEmployeeRoleEntities());
		allEntities.addAll(buildProfessionalLevelEntities());

		// Invariant singleton entity.
		allEntities.add(buildGuestEntity());

		// Dynamic domain entities.
		allEntities.addAll(buildEmployeeEntities());
		allEntities.addAll(buildGuardianEntities());
		allEntities.addAll(buildPatientEntities());
		allEntities.addAll(buildVisitEntities());

		// Action hierarchy (invariant; derived from the Cedar schema).
		allEntities.addAll(buildActionEntities());

		try {
			String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(allEntities);
			logger.debug("Rebuilt Cedar entities JSON ({} entities, {} characters).", allEntities.size(),
					json.length());
			return Entities.parse(json);
		}
		catch (JsonProcessingException exception) {
			throw new RuntimeException("Failed to serialize Cedar entities to JSON.", exception);
		}
		catch (Exception exception) {
			throw new RuntimeException("Failed to parse dynamically built Cedar entities.", exception);
		}
	}

	// Static reference entities (lookup tables).

	private List<Map<String, Object>> buildClinicEntities() {
		return jdbcTemplate.query("SELECT name FROM clinics", (rs, rowNum) -> {
			String clinicName = rs.getString("name");
			String cedarId = clinicName.replaceFirst("^Clinic\\s+", "");
			return buildSimpleEntity("Clinic", cedarId);
		});
	}

	private List<Map<String, Object>> buildGuardianAuthorityEntities() {
		return jdbcTemplate.query("SELECT name FROM authorities",
				(rs, rowNum) -> buildSimpleEntity("GuardianAuthority", rs.getString("name")));
	}

	private List<Map<String, Object>> buildConfidentialityEntities() {
		return jdbcTemplate.query("SELECT name FROM confidentialities",
				(rs, rowNum) -> buildSimpleEntity("Confidentiality", rs.getString("name")));
	}

	private List<Map<String, Object>> buildEmployeeRoleEntities() {
		return jdbcTemplate.query("SELECT name FROM roles",
				(rs, rowNum) -> buildSimpleEntity("EmployeeRole", rs.getString("name")));
	}

	private List<Map<String, Object>> buildProfessionalLevelEntities() {
		return jdbcTemplate.query("SELECT name FROM levels",
				(rs, rowNum) -> buildSimpleEntity("ProfessionalLevel", rs.getString("name")));
	}

	// Invariant singleton entity.

	private Map<String, Object> buildGuestEntity() {
		return buildSimpleEntity("Guest", "Unknown");
	}

	// Dynamic domain entities.

	private List<Map<String, Object>> buildEmployeeEntities() {
		// Retrieve all users (employees).
		List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT entity_id, username FROM users");

		// Pre-fetch all relational data into maps keyed by entity_id.
		Map<Integer, List<String>> clinicsByUser = fetchEntityClinics();
		Map<Integer, List<String>> rolesByUser = fetchUserRoles();
		Map<Integer, String> levelByUser = fetchUserLevels();
		Map<Integer, String> managerByUser = fetchUserManagers();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Map<String, Object> user : users) {
			Integer entityId = (Integer) user.get("entity_id");
			String username = (String) user.get("username");

			Map<String, Object> attrs = new LinkedHashMap<>();
			attrs.put("name", username);

			List<String> userClinics = clinicsByUser.getOrDefault(entityId, List.of());
			attrs.put("clinics", userClinics.stream().map(c -> entityRef("Clinic", c)).collect(Collectors.toList()));

			List<String> userRoles = rolesByUser.getOrDefault(entityId, List.of());
			attrs.put("roles", userRoles.stream().map(r -> entityRef("EmployeeRole", r)).collect(Collectors.toList()));

			String level = levelByUser.get(entityId);
			if (level != null) {
				attrs.put("level", entityRef("ProfessionalLevel", level));
			}

			String managerUsername = managerByUser.get(entityId);
			if (managerUsername != null) {
				attrs.put("manager", entityRef("Employee", managerUsername));
			}

			result.add(buildEntity("Employee", username, attrs));
		}
		return result;
	}

	private List<Map<String, Object>> buildGuardianEntities() {
		List<Map<String, Object>> guardians = jdbcTemplate.queryForList("SELECT a.entity_id, p.first_name, p.last_name "
				+ "FROM guardians a " + "JOIN persons p ON a.entity_id = p.entity_id");

		Map<Integer, List<String>> clinicsByEntity = fetchEntityClinics();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Map<String, Object> guardian : guardians) {
			Integer entityId = (Integer) guardian.get("entity_id");
			String name = guardian.get("first_name") + " " + guardian.get("last_name");

			Map<String, Object> attrs = new LinkedHashMap<>();
			attrs.put("name", name);

			List<String> guardianClinics = clinicsByEntity.getOrDefault(entityId, List.of());
			attrs.put("clinics", guardianClinics.stream().map(c -> entityRef("Clinic", c)).collect(Collectors.toList()));

			result.add(buildEntity("Guardian", name, attrs));
		}
		return result;
	}

	private List<Map<String, Object>> buildPatientEntities() {
		List<Map<String, Object>> patients = jdbcTemplate.queryForList("SELECT pt.entity_id, p.first_name, p.last_name "
				+ "FROM patients pt " + "JOIN persons p ON pt.entity_id = p.entity_id");

		Map<Integer, List<String>> clinicsByEntity = fetchEntityClinics();
		Map<Integer, List<Map<String, Object>>> guardiansByPatient = fetchPatientGuardians();
		Map<Integer, List<String>> doctorsByPatient = fetchPatientDoctors();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Map<String, Object> patient : patients) {
			Integer entityId = (Integer) patient.get("entity_id");
			String name = patient.get("first_name") + " " + patient.get("last_name");

			Map<String, Object> attrs = new LinkedHashMap<>();
			attrs.put("name", name);

			// Guardians attribute (set of { guardian, authority } records).
			List<Map<String, Object>> patientGuardians = guardiansByPatient.getOrDefault(entityId, List.of());
			List<Map<String, Object>> guardiansAttr = new ArrayList<>();
			for (Map<String, Object> pa : patientGuardians) {
				Map<String, Object> record = new LinkedHashMap<>();
				record.put("guardian", entityRef("Guardian", (String) pa.get("guardian_name")));
				record.put("authority", entityRef("GuardianAuthority", (String) pa.get("authority_name")));
				guardiansAttr.add(record);
			}
			attrs.put("guardians", guardiansAttr);

			List<String> patientClinics = clinicsByEntity.getOrDefault(entityId, List.of());
			attrs.put("clinics", patientClinics.stream().map(c -> entityRef("Clinic", c)).collect(Collectors.toList()));

			List<String> patientDoctorNames = doctorsByPatient.getOrDefault(entityId, List.of());
			attrs.put("doctors",
					patientDoctorNames.stream().map(d -> entityRef("Employee", d)).collect(Collectors.toList()));

			result.add(buildEntity("Patient", name, attrs));
		}
		return result;
	}

	private List<Map<String, Object>> buildVisitEntities() {
		List<Map<String, Object>> visits = jdbcTemplate
			.queryForList("SELECT v.entity_id, v.patient_id, v.confidentiality_id, "
					+ "p.first_name AS patient_first, p.last_name AS patient_last, " + "c.name AS confidentiality_name "
					+ "FROM visits v " + "JOIN patients pt ON v.patient_id = pt.entity_id "
					+ "JOIN persons p ON pt.entity_id = p.entity_id "
					+ "JOIN confidentialities c ON v.confidentiality_id = c.id "
					+ "ORDER BY v.patient_id, v.visit_date, v.entity_id");

		Map<Integer, List<String>> clinicsByEntity = fetchEntityClinics();
		Map<Integer, List<String>> guardiansByVisit = fetchVisitGuardians();
		Map<Integer, List<String>> doctorsByVisit = fetchVisitDoctors();

		// Compute per-patient sequence numbers for the Cedar Visit ID.
		Map<Integer, Integer> patientVisitCounter = new HashMap<>();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Map<String, Object> visit : visits) {
			Integer entityId = (Integer) visit.get("entity_id");
			Integer patientId = (Integer) visit.get("patient_id");
			String patientName = visit.get("patient_first") + " " + visit.get("patient_last");
			String confidentialityName = (String) visit.get("confidentiality_name");

			int sequence = patientVisitCounter.merge(patientId, 1, Integer::sum);
			String cedarVisitId = patientName + " " + sequence;

			Map<String, Object> attrs = new LinkedHashMap<>();

			attrs.put("patient", entityRef("Patient", patientName));

			List<String> visitGuardianNames = guardiansByVisit.getOrDefault(entityId, List.of());
			attrs.put("guardians",
					visitGuardianNames.stream().map(a -> entityRef("Guardian", a)).collect(Collectors.toList()));

			List<String> visitClinics = clinicsByEntity.getOrDefault(entityId, List.of());
			attrs.put("clinics", visitClinics.stream().map(c -> entityRef("Clinic", c)).collect(Collectors.toList()));

			List<String> visitDoctorNames = doctorsByVisit.getOrDefault(entityId, List.of());
			attrs.put("doctors",
					visitDoctorNames.stream().map(d -> entityRef("Employee", d)).collect(Collectors.toList()));

			attrs.put("confidentiality", entityRef("Confidentiality", confidentialityName));

			result.add(buildEntity("Visit", cedarVisitId, attrs));
		}
		return result;
	}

	// Action hierarchy (invariant; derived from the Cedar schema).

	private List<Map<String, Object>> buildActionEntities() {
		List<Map<String, Object>> actions = new ArrayList<>();

		actions.add(buildSimpleEntity("Action", "EmployeeOperations"));
		actions.add(buildSimpleEntity("Action", "PatientOperations"));

		actions.add(buildActionWithParents("ViewClinic", List.of("EmployeeOperations", "PatientOperations")));
		actions.add(buildActionWithParents("ListEmployees", List.of("EmployeeOperations")));
		actions.add(buildActionWithParents("AddEmployee", List.of("EmployeeOperations")));
		actions.add(buildActionWithParents("ViewEmployee", List.of("EmployeeOperations")));
		actions.add(buildActionWithParents("EditEmployee", List.of("EmployeeOperations")));
		actions.add(buildActionWithParents("DeleteEmployee", List.of("EmployeeOperations")));

		actions.add(buildActionWithParents("ListPatients", List.of("PatientOperations")));
		actions.add(buildActionWithParents("AddPatient", List.of("PatientOperations")));
		actions.add(buildActionWithParents("ViewPatient", List.of("PatientOperations")));
		actions.add(buildActionWithParents("EditPatient", List.of("PatientOperations")));
		actions.add(buildActionWithParents("DeletePatient", List.of("PatientOperations")));

		actions.add(buildActionWithParents("ListGuardians", List.of("PatientOperations")));
		actions.add(buildActionWithParents("AddGuardian", List.of("PatientOperations")));
		actions.add(buildActionWithParents("ViewGuardian", List.of("PatientOperations")));
		actions.add(buildActionWithParents("EditGuardian", List.of("PatientOperations")));
		actions.add(buildActionWithParents("DeleteGuardian", List.of("PatientOperations")));

		actions.add(buildActionWithParents("ListVisits", List.of("PatientOperations")));
		actions.add(buildActionWithParents("AddVisit", List.of("PatientOperations")));
		actions.add(buildActionWithParents("ViewVisit", List.of("PatientOperations")));
		actions.add(buildActionWithParents("EditVisit", List.of("PatientOperations")));
		actions.add(buildActionWithParents("DeleteVisit", List.of("PatientOperations")));

		return actions;
	}

	// Batch-fetching helpers (relational join data).

	private Map<Integer, List<String>> fetchEntityClinics() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT ec.entity_id, c.name "
				+ "FROM entity_clinics ec " + "JOIN clinics c ON ec.clinic_id = c.clinic_id");

		return rows.stream()
			.collect(Collectors.groupingBy(row -> (Integer) row.get("entity_id"), Collectors
				.mapping(row -> ((String) row.get("name")).replaceFirst("^Clinic\\s+", ""), Collectors.toList())));
	}

	private Map<Integer, List<String>> fetchUserRoles() {
		List<Map<String, Object>> rows = jdbcTemplate
			.queryForList("SELECT ur.user_id, r.name " + "FROM user_roles ur " + "JOIN roles r ON ur.role_id = r.id");

		return rows.stream()
			.collect(Collectors.groupingBy(row -> (Integer) row.get("user_id"),
					Collectors.mapping(row -> (String) row.get("name"), Collectors.toList())));
	}

	private Map<Integer, String> fetchUserLevels() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(
				"SELECT ul.user_id, l.name " + "FROM user_levels ul " + "JOIN levels l ON ul.level_id = l.id");

		Map<Integer, String> result = new HashMap<>();
		for (Map<String, Object> row : rows) {
			result.putIfAbsent((Integer) row.get("user_id"), (String) row.get("name"));
		}
		return result;
	}

	private Map<Integer, String> fetchUserManagers() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT um.user_id, u.username "
				+ "FROM user_manager um " + "JOIN users u ON um.manager_id = u.entity_id");

		Map<Integer, String> result = new HashMap<>();
		for (Map<String, Object> row : rows) {
			result.putIfAbsent((Integer) row.get("user_id"), (String) row.get("username"));
		}
		return result;
	}

	private Map<Integer, List<Map<String, Object>>> fetchPatientGuardians() {
		List<Map<String, Object>> rows = jdbcTemplate
			.queryForList("SELECT pa.patient_id, " + "CONCAT(p.first_name, ' ', p.last_name) AS guardian_name, "
					+ "auth.name AS authority_name " + "FROM patient_guardians pa "
					+ "JOIN guardians a ON pa.guardian_id = a.entity_id " + "JOIN persons p ON a.entity_id = p.entity_id "
					+ "JOIN authorities auth ON pa.authority_id = auth.id");

		return rows.stream()
			.collect(Collectors.groupingBy(row -> (Integer) row.get("patient_id"), Collectors.mapping(row -> {
				Map<String, Object> entry = new HashMap<>();
				entry.put("guardian_name", row.get("guardian_name"));
				entry.put("authority_name", row.get("authority_name"));
				return entry;
			}, Collectors.toList())));
	}

	private Map<Integer, List<String>> fetchPatientDoctors() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT pd.patient_id, u.username "
				+ "FROM patient_doctors pd " + "JOIN users u ON pd.doctor_id = u.entity_id");

		return rows.stream()
			.collect(Collectors.groupingBy(row -> (Integer) row.get("patient_id"),
					Collectors.mapping(row -> (String) row.get("username"), Collectors.toList())));
	}

	private Map<Integer, List<String>> fetchVisitGuardians() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT va.visit_id, "
				+ "CONCAT(p.first_name, ' ', p.last_name) AS guardian_name " + "FROM visit_guardians va "
				+ "JOIN guardians a ON va.guardian_id = a.entity_id " + "JOIN persons p ON a.entity_id = p.entity_id");

		return rows.stream()
			.collect(Collectors.groupingBy(row -> (Integer) row.get("visit_id"),
					Collectors.mapping(row -> (String) row.get("guardian_name"), Collectors.toList())));
	}

	private Map<Integer, List<String>> fetchVisitDoctors() {
		List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT vd.visit_id, u.username "
				+ "FROM visit_doctors vd " + "JOIN users u ON vd.doctor_id = u.entity_id");

		return rows.stream()
			.collect(Collectors.groupingBy(row -> (Integer) row.get("visit_id"),
					Collectors.mapping(row -> (String) row.get("username"), Collectors.toList())));
	}

	// JSON structure helpers.

	// Builds a Cedar entity JSON structure with no attributes and no parents.
	private Map<String, Object> buildSimpleEntity(String type, String id) {
		return buildEntity(type, id, Map.of());
	}

	// Builds a Cedar entity JSON structure with the given attributes and no parents.
	private Map<String, Object> buildEntity(String type, String id, Map<String, Object> attrs) {
		Map<String, Object> entity = new LinkedHashMap<>();
		entity.put("uid", Map.of("type", NAMESPACE + "::" + type, "id", id));
		entity.put("attrs", attrs);
		entity.put("parents", List.of());
		return entity;
	}

	// Builds an Action entity with the specified parent action entities.
	private Map<String, Object> buildActionWithParents(String actionId, List<String> parentActionIds) {
		Map<String, Object> entity = new LinkedHashMap<>();
		entity.put("uid", Map.of("type", NAMESPACE + "::Action", "id", actionId));
		entity.put("attrs", Map.of());
		entity.put("parents",
				parentActionIds.stream().map(parentId -> entityRef("Action", parentId)).collect(Collectors.toList()));
		return entity;
	}

	// Builds a Cedar entity reference using the "__entity" format required by the Cedar
	// JSON entity specification.
	private Map<String, Object> entityRef(String type, String id) {
		return Map.of("__entity", Map.of("type", NAMESPACE + "::" + type, "id", id));
	}

}
