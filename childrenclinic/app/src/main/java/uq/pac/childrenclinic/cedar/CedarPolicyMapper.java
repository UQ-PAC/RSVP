/*
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
 */

package uq.pac.childrenclinic.cedar;

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
