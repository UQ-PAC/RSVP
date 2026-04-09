package org.springframework.samples.petclinic.cedar;

import com.cedarpolicy.value.PrimString;
import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Aspect
@Component
public class CedarAspect {

	private final CedarService cedarService;

	private final JdbcTemplate jdbcTemplate;

	private static final String USER_ID_SESSION_ATTRIBUTE = "currentUser";

	private static final String CONTEXT_PARAM_PREFIX = "cedar-context-";

	private record ResourceMetadata(String sqlQuery, Function<Map<String, Object>, String> nameExtractor) {
	}

	private static final Map<String, ResourceMetadata> RESOURCE_REGISTRY = Map.of("PetOwner",
			new ResourceMetadata("SELECT first_name, last_name FROM owners WHERE entity_id = ?",
					rs -> rs.get("first_name") + " " + rs.get("last_name")),
			"Veterinarian",
			new ResourceMetadata("SELECT first_name, last_name FROM vets WHERE entity_id = ?",
					rs -> rs.get("first_name") + " " + rs.get("last_name")),
			"Pet", new ResourceMetadata("SELECT name FROM pets WHERE entity_id = ?", rs -> rs.get("name").toString()),
			"PetVisit", new ResourceMetadata("SELECT description FROM visits WHERE entity_id = ?",
					rs -> rs.get("description").toString()));

	public CedarAspect(CedarService cedarService, JdbcTemplate jdbcTemplate) {
		this.cedarService = cedarService;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Before("@annotation(requiresAuthorization)")
	public void enforceAuthorization(JoinPoint joinPoint, CedarAuthorization requiresAuthorization) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
			.getRequest();

		String principalId = extractPrincipalFromSession(request);

		// Prints requests and cookie to console.
		System.out
			.println(System.lineSeparator() + "Cedar request resourceType: " + requiresAuthorization.resourceType());

		if (requiresAuthorization.resourceId().equals("")) {
			System.out.println("HTTP request resourceId: " + extractResourceId(request));
		} else {
			System.out.println("Cedar request resourceId: " + requiresAuthorization.resourceId());
		}

		System.out.println("Cookie principalId: " + principalId);

		EntityUID principal;
		if (principalId.equals("Guest")) {
			principal = EntityUID.parse("PetClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			principal = EntityUID.parse("PetClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}

		EntityUID action = EntityUID.parse("PetClinic::Action::\"" + requiresAuthorization.action() + "\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));

		EntityUID resource = resolveResourceUid(request, requiresAuthorization);

		// Populate context for Attribute-Based Access Control (ABAC).
		Map<String, Value> contextMap = new HashMap<>();
		contextMap.putAll(extractContextFromParameters(request, principalId));

		// Prints Cedar context to console.
		System.out.println("Cedar context: " + contextMap.toString());

		boolean validateRequest = requiresAuthorization.validate();

		CedarRequest authorizationRequest = new CedarRequest(principal, action, resource, contextMap, validateRequest);

		ResponseEntity<String> response = cedarService.checkAccess(authorizationRequest);

		// Prints Cedar response to console.
		System.out.println("Cedar response status code: " + response.getStatusCode());
		System.out.println("Cedar response body: " + response.getBody());

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null
				|| !response.getBody().startsWith("Access Granted.")) {
			String prefix = """
					Access Denied.
					""";
			String body = """
					Access Denied by the Cedar Policy Engine.

					%s
					""".formatted(response.getBody().replaceAll("(?m)^" + prefix, ""));
			throw new CedarDeniedException(body);
		}
	}

	private String extractPrincipalFromSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute(USER_ID_SESSION_ATTRIBUTE) != null) {
			return (String) session.getAttribute(USER_ID_SESSION_ATTRIBUTE);
		}
		return "Guest";
	}

	private String extractResourceId(HttpServletRequest request) {
		String path = request.getRequestURI();
		String[] segments = path.split("/");
		return segments.length > 0 ? segments[segments.length - 1] : "global";
	}

	private EntityUID resolveResourceUid(HttpServletRequest request, CedarAuthorization requiresAuthorization) {
		String resourceType = requiresAuthorization.resourceType();
		String resourceIdentifier = extractResourceId(request);

		ResourceMetadata metadata = RESOURCE_REGISTRY.get(resourceType);

		if (metadata != null) {
			@SuppressWarnings("unchecked")
			Map<String, String> pathVariables = (Map<String, String>) request
				.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

			if (pathVariables != null && !pathVariables.isEmpty()) {
				String expectedKey = resourceType.toLowerCase() + "Id";
				String entityIdString = pathVariables.get(expectedKey);

				if (entityIdString == null) {
					entityIdString = pathVariables.entrySet()
						.stream()
						.filter(entry -> entry.getKey().toLowerCase().endsWith("id"))
						.map(Map.Entry::getValue)
						.findFirst()
						.orElse(null);
				}

				if (entityIdString != null) {
					try {
						int entityId = Integer.parseInt(entityIdString);

						Map<String, Object> queryResult = this.jdbcTemplate.queryForMap(metadata.sqlQuery(), entityId);
						String resolvedName = metadata.nameExtractor().apply(queryResult);

						if (resolvedName != null && !resolvedName.trim().isEmpty()) {
							resourceIdentifier = resolvedName.trim();
						}
					}
					catch (NumberFormatException | EmptyResultDataAccessException | NullPointerException exception) {
						// Retains the default numeric identifier if parsing, resolution,
						// or formatting fails.
					}
				}
			}
		}

		final String finalResourceIdentifier = resourceIdentifier;

		return EntityUID.parse("PetClinic::" + resourceType + "::\"" + finalResourceIdentifier + "\"")
			.orElseThrow(() -> new IllegalArgumentException(
					"Invalid Resource UID format generated for: " + finalResourceIdentifier));
	}

	private Map<String, Value> extractContextFromParameters(HttpServletRequest request, String principalId) {
		Map<String, Value> context = new HashMap<>();
		Enumeration<String> parameterNames = request.getParameterNames();

		if (parameterNames != null) {
			while (parameterNames.hasMoreElements()) {
				String paramName = parameterNames.nextElement();
				if (paramName.regionMatches(true, 0, CONTEXT_PARAM_PREFIX, 0, CONTEXT_PARAM_PREFIX.length())) {
					String contextKey = paramName.substring(CONTEXT_PARAM_PREFIX.length()).toLowerCase();

					if ("authenticated".equals(contextKey)) {
						boolean isAuthenticated = false;
						if (!"Guest".equals(principalId)) {
							try {
								String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
								Integer count = this.jdbcTemplate.queryForObject(sql, Integer.class, principalId);
								isAuthenticated = (count != null && count > 0);
							}
							catch (Exception exception) {
								isAuthenticated = false;
							}
						}
						context.put(contextKey, new PrimString(String.valueOf(isAuthenticated)));
					}
					else {
						String paramValue = request.getParameter(paramName);
						context.put(contextKey, new PrimString(paramValue));
					}
				}
			}
		}
		return context;
	}

}
