package uq.pac.childrenclinic.cedar;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.model.AuthorizationResponse;
import com.cedarpolicy.model.AuthorizationSuccessResponse;
import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.policy.PolicySet;
import com.cedarpolicy.model.schema.Schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CedarService {

	private final AuthorizationEngine engine;

	private final PolicySet policySet;

	private final Entities entities;

	private final Schema schema;

	private Map<String, String> policyIdMap;

	private static final Logger logger = LoggerFactory.getLogger(CedarService.class);

	public CedarService(@Value("${policy.file:childrenclinic-rsvp-policy.cedar}") String policyFile) {
		try {
			this.policySet = PolicySet.parsePolicies(Path.of("src/main/resources/cedar/" + policyFile));
			logger.info("Cedar Policy file loaded: {}", policyFile);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse Cedar Policy.", e);
		}
		try {
			this.entities = Entities.parse(Path.of("src/main/resources/cedar/childrenclinic-rsvp-entities.json"));
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse Cedar Entities.", e);
		}
		try {
			String schemaText = Files
				.readString(Path.of("src/main/resources/cedar/childrenclinic-rsvp-schema.cedarschema"));
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
				parsedRequest.getResource(), parsedRequest.getContext(), Optional.ofNullable(this.schema),
				parsedRequest.isValidateRequest());

		try {
			AuthorizationResponse response = engine.isAuthorized(request, this.policySet, this.entities);

			logger.info("Cedar request: {principal = " + request.principalEUID + ", action = " + request.actionEUID
					+ ", resource = " + request.resourceEUID + ", context = " + request.context + ", validateRequest = "
					+ request.enableRequestValidation + "}");

			logger.info("Cedar raw response: " + response.toString());

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
					logger.info("Cedar response status code: 200 OK");
					logger.debug("Cedar response body: \n{}", body);
					return ResponseEntity.ok(body);
				}
				else {
					String joinedReasons = String.join("\n", resolvedReasons);
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
				throw new Exception(response.errors.toString());
			}
		}
		catch (Exception e) {
			logger.error("Authorization check failed: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError().body("Authorization check failed: " + e.getMessage());
		}
	}

}
