package org.springframework.samples.petclinic.cedar;

import com.cedarpolicy.value.PrimString;
import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class CedarAspect {

	private final CedarService cedarService;

	private static final String USER_ID_SESSION_ATTRIBUTE = "currentUser";

	private static final String CONTEXT_PARAM_PREFIX = "cedar-context-";

	public CedarAspect(CedarService cedarService) {
		this.cedarService = cedarService;
	}

	@Before("@annotation(requiresAuthorization)")
	public void enforceAuthorization(JoinPoint joinPoint, CedarAuthorization requiresAuthorization) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
			.getRequest();

		String principalId = extractPrincipalFromSession(request);

		// Prints requests and cookie to console.
		System.out.println("Cedar request resourceType: " + requiresAuthorization.resourceType());
		System.out.println("HTTP request resourceId: " + extractResourceId(request));
		System.out.println("Cookie principalId: " + principalId);

		EntityUID principal = EntityUID.parse("PetClinic::User::\"" + principalId + "\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		EntityUID action = EntityUID.parse("PetClinic::Action::\"" + requiresAuthorization.action() + "\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
		EntityUID resource = EntityUID
			.parse("PetClinic::" + requiresAuthorization.resourceType())
			.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

		// Populate context for Attribute-Based Access Control (ABAC).
		Map<String, Value> contextMap = extractContextFromParameters(request);

		boolean validateRequest = requiresAuthorization.validate();

		CedarRequest authorizationRequest = new CedarRequest(principal, action, resource, contextMap, validateRequest);

		ResponseEntity<String> response = cedarService.checkAccess(authorizationRequest);

		// Prints Cedar response to console.
		System.out.println("Cedar response status code: " + response.getStatusCode());
		System.out.println("Cedar response body: " + response.getBody());

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null
				|| !response.getBody().equals("Access Granted.")) {
			throw new CedarDeniedException("Access Denied by the Cedar Policy Engine.");
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

	private Map<String, Value> extractContextFromParameters(HttpServletRequest request) {
		Map<String, Value> context = new HashMap<>();
		Enumeration<String> parameterNames = request.getParameterNames();

		if (parameterNames != null) {
			while (parameterNames.hasMoreElements()) {
				String paramName = parameterNames.nextElement();
				if (paramName.regionMatches(true, 0, CONTEXT_PARAM_PREFIX, 0, CONTEXT_PARAM_PREFIX.length())) {
					String contextKey = paramName.substring(CONTEXT_PARAM_PREFIX.length()).toLowerCase();
					String paramValue = request.getParameter(paramName);
					context.put(contextKey, new PrimString(paramValue));
				}
			}
		}
		return context;
	}

}
