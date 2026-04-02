package org.springframework.samples.petclinic.cedar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CedarPolicyMapper {

	public static Map<String, String> mapEngineIdsToAnnotations(String policyDocument) {
		Map<String, String> policyAnnotationMap = new HashMap<>();

		String sanitizedDocument = policyDocument.replaceAll("//.*", "");

		Pattern policyBlockPattern = Pattern.compile("(?s)(.*?)\\b(permit|forbid)\\b");
		Matcher blockMatcher = policyBlockPattern.matcher(sanitizedDocument);

		Pattern annotationPattern = Pattern.compile("@id\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");

		int policyIndex = 0;
		while (blockMatcher.find()) {
			String blockContent = blockMatcher.group(1);
			Matcher annotationMatcher = annotationPattern.matcher(blockContent);

			String annotationId = null;
			while (annotationMatcher.find()) {
				annotationId = annotationMatcher.group(1);
			}

			if (annotationId != null) {
				policyAnnotationMap.put("policy" + policyIndex, annotationId);
			}

			policyIndex++;
		}

		return policyAnnotationMap;
	}

	public static Set<String> resolveReasons(Set<String> engineReasons, Map<String, String> annotationMap) {
		if (engineReasons == null || engineReasons.isEmpty()) {
			return Set.of();
		}

		return engineReasons.stream()
			.map(reason -> annotationMap.getOrDefault(reason, reason))
			.collect(Collectors.toSet());
	}

}
