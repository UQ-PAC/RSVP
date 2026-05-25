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

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.model.AuthorizationResponse;
import com.cedarpolicy.model.AuthorizationSuccessResponse;
import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.model.schema.Schema;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CedarService {

	private final AuthorizationEngine engine;

	private final Path policyPath;

	private final Path schemaPath;

	private final CedarEntityBuilder cedarEntityBuilder;

	private final AtomicReference<Entities> entitiesCache = new AtomicReference<>();

	private final CedarRequestScopedCache requestCache;

	private static final Logger logger = LoggerFactory.getLogger(CedarService.class);

	public CedarService(@Value("${policy.file:childrenclinic.cedar}") String policyFile,
			CedarEntityBuilder cedarEntityBuilder, CedarRequestScopedCache requestCache) {
		this.policyPath = Path.of("src/main/resources/cedar/" + policyFile);
		this.schemaPath = Path.of("src/main/resources/cedar/childrenclinic.cedarschema");
		this.cedarEntityBuilder = cedarEntityBuilder;
		this.requestCache = requestCache;

		try {
			this.engine = new BasicAuthorizationEngine();
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize Cedar Engine.", e);
		}
	}

	private Entities getEntities() {
		Entities current = entitiesCache.get();
		if (current == null) {
			logger.info("Cedar entities cache is empty; rebuilding from the database.");
			current = cedarEntityBuilder.buildEntities();
			entitiesCache.set(current);
		}
		return current;
	}

	public void invalidateEntities() {
		logger.info("Cedar entities cache invalidated.");
		entitiesCache.set(null);
	}

	@EventListener
	public void onEntitiesInvalidation(CedarEntitiesInvalidationEvent event) {
		invalidateEntities();
	}

	public ResponseEntity<String> checkAccess(@RequestBody CedarRequest parsedRequest) {
		this.requestCache.ensureLoaded(this.policyPath, this.schemaPath);

		PolicySet policySet = this.requestCache.getPolicySet();
		Schema schema = this.requestCache.getSchema();
		Map<String, String> policyIdMap = this.requestCache.getPolicyIdMap();

		AuthorizationRequest request = new AuthorizationRequest(parsedRequest.getPrincipal(), parsedRequest.getAction(),
				parsedRequest.getResource(), parsedRequest.getContext(), Optional.ofNullable(schema),
				parsedRequest.isValidateRequest());

		try {
			AuthorizationResponse response = engine.isAuthorized(request, policySet, getEntities());

			logger.info("Cedar request: {principal = " + request.principalEUID + ", action = " + request.actionEUID
					+ ", resource = " + request.resourceEUID + ", context = " + request.context + ", validateRequest = "
					+ request.enableRequestValidation + "}");

			logger.info("Cedar raw response: " + response.toString());

			if (response.type == AuthorizationResponse.SuccessOrFailure.Success) {
				boolean isAllowed = false;
				Set<String> resolvedReasons = new HashSet<>();
				if (response.success.isPresent()) {
					AuthorizationSuccessResponse successResponse = response.success.get();
					isAllowed = successResponse.isAllowed();
					Set<String> reasons = successResponse.getReason();
					resolvedReasons = CedarPolicyMapper.resolveReasons(reasons, policyIdMap);
				}

				String joinedReasons = String.join("\n", resolvedReasons);

				if (isAllowed) {
					String body = """
							Access Granted.
							%s
							""".formatted(joinedReasons);
					logger.info("Cedar response status code: 200 OK");
					logger.debug("Cedar response body: \n{}", body);
					return ResponseEntity.ok(body);
				}
				else {
					String body = """
							Access Denied.
							%s
							""".formatted(joinedReasons);
					logger.warn("Cedar response status code: 403 FORBIDDEN");
					logger.debug("Cedar response body: \n{}", body);
					return ResponseEntity.status(403).body(body);
				}
			}
			else {
				String errorDetails = response.toString();
				logger.error("Cedar policy evaluation failed: {}", errorDetails);
				return ResponseEntity.internalServerError().body("Authorization check failed: " + errorDetails);
			}
		}
		catch (Exception e) {
			logger.error("Authorization check failed: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Authorization check failed: " + e.getMessage());
		}
	}

}
