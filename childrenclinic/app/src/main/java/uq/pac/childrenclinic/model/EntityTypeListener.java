package uq.pac.childrenclinic.model;

import jakarta.persistence.PostPersist;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import uq.pac.childrenclinic.system.Role;
import uq.pac.childrenclinic.system.User;

@Component
public class EntityTypeListener {

	private static final Logger logger = LoggerFactory.getLogger(EntityTypeListener.class);

	private final JdbcTemplate jdbcTemplate;

	// Maps each concrete entity class name to the entity_type label used in the
	// entity_types table.
	private static final Map<String, String> TYPE_LABELS = Map.of("User", "User", "Administrative Assistant",
			"Administrative Assistant", "Doctor", "Doctor", "Adult", "Adult", "Patient", "Patient", "Visit", "Visit");

	private static final String ADMINISTRATOR_ROLE_NAME = "Administrator";

	public EntityTypeListener(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostPersist
	public void insertEntityType(Object entity) {
		if (!(entity instanceof BaseEntity baseEntity)) {
			return;
		}

		Integer entityId = baseEntity.getId();
		if (entityId == null) {
			logger.warn("PostPersist fired with null entity_id for {}; skipping entity_types insert.",
					entity.getClass().getSimpleName());
			return;
		}

		String className = entity.getClass().getSimpleName();
		String typeLabel = TYPE_LABELS.get(className);

		if (typeLabel != null) {
			jdbcTemplate.update("INSERT INTO entity_types (entity_id, entity_type) VALUES (?, ?)", entityId, typeLabel);
		}

		if (entity instanceof User user) {
			Set<Role> roles = user.getRoles();
			if (roles != null) {
				boolean isAdministrator = roles.stream()
					.anyMatch(role -> ADMINISTRATOR_ROLE_NAME.equals(role.getName()));
				if (isAdministrator) {
					jdbcTemplate.update("INSERT INTO entity_types (entity_id, entity_type) VALUES (?, ?)", entityId,
							ADMINISTRATOR_ROLE_NAME);
				}
			}
		}

	}

}
