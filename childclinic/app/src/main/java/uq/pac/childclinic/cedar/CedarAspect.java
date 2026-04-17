package uq.pac.childclinic.cedar;

import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.PrimString;
import com.cedarpolicy.value.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

@Aspect
@Component
public class CedarAspect {

	private final CedarService cedarService;

	private final JdbcTemplate jdbcTemplate;

	private static final String USER_ID_SESSION_ATTRIBUTE = "currentUser";

	private static final String CONTEXT_PARAM_PREFIX = "cedar-context-";

	private record ResourceMetadata(String sqlQuery, Function<Map<String, Object>, String> nameExtractor) {
	}

	private static final Map<String, ResourceMetadata> RESOURCE_REGISTRY = Map.of("Parent",
			new ResourceMetadata("SELECT first_name, last_name FROM parents WHERE entity_id = ?",
					rs -> rs.get("first_name") + " " + rs.get("last_name")),
			"Doctor",
			new ResourceMetadata("SELECT first_name, last_name FROM doctors WHERE entity_id = ?",
					rs -> rs.get("first_name") + " " + rs.get("last_name")),
			"Child", new ResourceMetadata("SELECT name FROM children WHERE entity_id = ?", rs -> rs.get("name").toString()),
			"Visit", new ResourceMetadata("SELECT description FROM visits WHERE entity_id = ?",
					rs -> rs.get("description").toString()));

	public CedarAspect(CedarService cedarService, JdbcTemplate jdbcTemplate) {
		this.cedarService = cedarService;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Before("@annotation(CedarAuthorization) || @annotation(CedarAuthorizations)")
	public void enforceAuthorization(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		CedarAuthorization[] annotations = method.getAnnotationsByType(CedarAuthorization.class);

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
			.getRequest();

		String principalId = extractPrincipalFromSession(request);

		System.out.println("Cookie principalId: " + principalId);

		EntityUID principal;
		if (principalId.equals("Guest")) {
			principal = EntityUID.parse("ChildClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			principal = EntityUID.parse("ChildClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}

		// Populate context for Attribute-Based Access Control (ABAC).
		Map<String, Value> contextMap = new HashMap<>();
		contextMap.putAll(extractContextFromParameters(request, principalId));

		// Prints Cedar context to console.
		System.out.println("Cedar context: " + contextMap.toString());

		for (CedarAuthorization requiresAuthorization : annotations) {
			// Prints requests and cookie to console.
			System.out
				.println(System.lineSeparator() + "Cedar request resourceType: " + requiresAuthorization.resourceType());

			if (requiresAuthorization.resourceId().equals("")) {
				System.out.println("HTTP request resourceId: " + extractResourceId(request, requiresAuthorization.resourceType()));
				System.out.println("Cedar resource: " + resolveResourceUid(request, requiresAuthorization).toString());
			}
			else {
				System.out.println("Cedar request resourceId: " + requiresAuthorization.resourceId());
				System.out.println("HTTP resource: " + extractResourceId(request, requiresAuthorization.resourceType()));
			}

            EntityUID action = EntityUID.parse("ChildClinic::Action::\"" + requiresAuthorization.action() + "\"")
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));

            EntityUID resource = resolveResourceUid(request, requiresAuthorization);

            boolean validateRequest = requiresAuthorization.validate();

            CedarRequest authorizationRequest = new CedarRequest(principal, action, resource, contextMap, validateRequest);

			System.out.println("Cedar request: " + authorizationRequest.toString());

            ResponseEntity<String> response = cedarService.checkAccess(authorizationRequest);

			// Prints Cedar response to console.
			System.out.println("Cedar response status code: " + response.getStatusCode());
			System.out.println("Cedar response body: " + response.getBody());

            // Any single rejection immediately terminates the invocation
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || !response.getBody().startsWith("Access Granted.")) {
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
	}

	private String extractPrincipalFromSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute(USER_ID_SESSION_ATTRIBUTE) != null) {
			return (String) session.getAttribute(USER_ID_SESSION_ATTRIBUTE);
		}
		return "Guest";
	}

	private String extractResourceId(HttpServletRequest request, String resourceType) {
		@SuppressWarnings("unchecked")
		Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		if (pathVariables != null && !pathVariables.isEmpty()) {
			String expectedKey = resourceType.toLowerCase() + "Id";

			if (pathVariables.containsKey(expectedKey)) {
				return pathVariables.get(expectedKey);
			}

			String entityIdString = pathVariables.get(expectedKey);

			if (entityIdString == null) {
				entityIdString = pathVariables.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().endsWith("id")).map(Map.Entry::getValue).findFirst().orElse(null);
			}

			if (entityIdString != null) {
				return entityIdString;
			}
		}

		String path = request.getRequestURI();
		String[] segments = path.split("/");
		return segments.length > 0 ? segments[segments.length - 1] : "global";
	}

	private EntityUID resolveResourceUid(HttpServletRequest request, CedarAuthorization requiresAuthorization) {
		String authorizationResourceType = requiresAuthorization.resourceType();

		// String authorizationResourceId = requiresAuthorization.resourceId();

		// if (authorizationResourceId != "") {
		// 	return EntityUID.parse("ChildClinic::" + authorizationResourceType + "::\"" + authorizationResourceId + "\"")
		// 		.orElseThrow(() -> new IllegalArgumentException(
		// 				"Invalid Resource UID format generated for: " + authorizationResourceId + "."));
		// }

		String requestResourceIdentifier = extractResourceId(request, authorizationResourceType);

		ResourceMetadata metadata = RESOURCE_REGISTRY.get(authorizationResourceType);

		if (metadata != null && !requestResourceIdentifier.equals("global")) {

			try {
				int entityId = Integer.parseInt(requestResourceIdentifier);

				Map<String, Object> queryResult = this.jdbcTemplate.queryForMap(metadata.sqlQuery(), entityId);
				String resolvedName = metadata.nameExtractor().apply(queryResult);

				if (resolvedName != null && !resolvedName.trim().isEmpty()) {
					requestResourceIdentifier = resolvedName.trim();
				}
			}
			catch (NumberFormatException | EmptyResultDataAccessException | NullPointerException exception) {
				// Retains the default numeric identifier if parsing, resolution, or formatting fails.
			}

		}

		final String finalResourceIdentifier = requestResourceIdentifier;

		return EntityUID.parse("ChildClinic::" + authorizationResourceType + "::\"" + finalResourceIdentifier + "\"")
			.orElseThrow(() -> new IllegalArgumentException(
					"Invalid Resource UID format generated for: " + finalResourceIdentifier + "."));
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
