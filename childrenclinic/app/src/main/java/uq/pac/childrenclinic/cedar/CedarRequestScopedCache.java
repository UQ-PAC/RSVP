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

import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.model.schema.Schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CedarRequestScopedCache {

	private static final Logger logger = LoggerFactory.getLogger(CedarRequestScopedCache.class);

	private PolicySet policySet;

	private Schema schema;

	private Map<String, String> policyIdMap;

	private boolean loaded = false;

	// Loads the policy set, schema, and policy ID map from disk on the first invocation
	// within a given HTTP request.
	// Subsequent invocations within the same request return the cached instances.
	// The whole bean is discarded at the end of each request, ensuring that the next
	// request re-reads the files from disk.
	public synchronized void ensureLoaded(Path policyPath, Path schemaPath) {
		if (this.loaded) {
			return;
		}

		try {
			this.policySet = PolicySet.parsePolicies(policyPath);
			logger.info("Cedar Policy file loaded for this request: {}", policyPath);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse Cedar Policy.", e);
		}

		try {
			String schemaText = Files.readString(schemaPath);
			this.schema = Schema.parse(Schema.JsonOrCedar.Cedar, schemaText);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse Cedar Schema.", e);
		}

		try {
			String policyContent = Files.readString(policyPath);
			this.policyIdMap = CedarPolicyMapper.mapEngineIdsToAnnotations(policyContent);
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed to read Cedar policy file for annotations mapping.", e);
		}

		this.loaded = true;
	}

	public PolicySet getPolicySet() {
		return this.policySet;
	}

	public Schema getSchema() {
		return this.schema;
	}

	public Map<String, String> getPolicyIdMap() {
		return this.policyIdMap;
	}

}
