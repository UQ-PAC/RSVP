package org.springframework.samples.petclinic.cedar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.model.AuthorizationResponse;
import com.cedarpolicy.model.AuthorizationSuccessResponse;
import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.model.schema.Schema;

@Service
public class CedarService {

	private final AuthorizationEngine engine;

	private final PolicySet policySet;

	private final Entities entities;

	private final Schema schema;

	private Map<String, String> policyIdMap;

	public CedarService(@Value("${policy.file:petclinic-rsvp-policy.cedar}") String policyFile) {
		try {
			this.policySet = PolicySet.parsePolicies(Path.of("src/main/resources/cedar/" + policyFile));
			System.out
				.println(System.lineSeparator() + "Cedar Policy file loaded: " + policyFile + System.lineSeparator());
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse Cedar Policy.", e);
		}
		try {
			this.entities = Entities.parse(Path.of("src/main/resources/cedar/petclinic-rsvp-entities.json"));
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse Cedar Entities.", e);
		}
		try {
			String schemaText = Files.readString(Path.of("src/main/resources/cedar/petclinic-rsvp-schema.cedarschema"));
			this.schema = Schema.parse(Schema.JsonOrCedar.Cedar, schemaText);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse Cedar Schema.", e);
		}
		try {
			this.engine = new BasicAuthorizationEngine();
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize Cedar Engine.", e);
		}
		try {
			String policyContent = Files.readString(Path.of("src/main/resources/cedar/" + policyFile));
			this.policyIdMap = CedarPolicyMapper.mapEngineIdsToAnnotations(policyContent);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Failed to read Cedar policy file for annotations mapping.", exception);
		}
	}

	public ResponseEntity<String> checkAccess(@RequestBody CedarRequest parsedRequest) {
		AuthorizationRequest request = new AuthorizationRequest(parsedRequest.getPrincipal(), parsedRequest.getAction(),
				parsedRequest.getResource(), parsedRequest.getContext(), Optional.ofNullable(schema),
				parsedRequest.isValidateRequest());

		try {
			AuthorizationResponse response = engine.isAuthorized(request, this.policySet, this.entities);

			// Prints Cedar decision to console.
			System.out.println(
					System.lineSeparator() + "Cedar raw response: " + response.toString() + System.lineSeparator());

			if (response.type == AuthorizationResponse.SuccessOrFailure.Success) {
				boolean isAllowed = false;
				Set<String> resolvedReasons = new HashSet<>();
				List<AuthorizationSuccessResponse.AuthorizationError> errors = new ArrayList<>();
				if (response.success.isPresent()) {
					AuthorizationSuccessResponse successResponse = response.success.get();
					isAllowed = successResponse.isAllowed();
					Set<String> reasons = successResponse.getReason();
					errors = successResponse.getErrors();
					resolvedReasons = CedarPolicyMapper.resolveReasons(reasons, this.policyIdMap);
				}

				if (isAllowed) {
					String joinedReasons = String.join("\n", resolvedReasons);
					String body = """
							Access Granted.
							%s
							""".formatted(joinedReasons);
					return ResponseEntity.ok(body);
				}
				else if (!isAllowed) {
					String joinedReasons = String.join("\n", resolvedReasons);
					String body = """
							Access Denied.
							%s
							""".formatted(joinedReasons);
					return ResponseEntity.status(403).body(body);
				}
				else {
					throw new Exception(errors.toString());
				}
			}
			else {
				throw new Exception(response.errors.toString());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Authorization check failed: " + e.getMessage());
		}
	}

}
