package org.springframework.samples.petclinic.cedar;

import com.cedarpolicy.AuthorizationEngine;
import com.cedarpolicy.BasicAuthorizationEngine;
import com.cedarpolicy.model.AuthorizationRequest;
import com.cedarpolicy.model.AuthorizationResponse;
import com.cedarpolicy.model.schema.Schema;
import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.policy.PolicySet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class CedarService {

	private final AuthorizationEngine engine;

	private final PolicySet policySet;

	private final Entities entities;

	private final Schema schema;

	public CedarService(@Value("${policy.file:petclinic-rsvp-policy.cedar}") String policyFile) {
		try {
			this.policySet = PolicySet
				.parsePolicies(Path.of("src/main/resources/cedar/" + policyFile));
			System.out.println(System.lineSeparator() + "Cedar Policy loaded: " + policyFile + System.lineSeparator());
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
			String schemaText = Files
				.readString(Path.of("src/main/resources/cedar/petclinic-rsvp-schema.cedarschema"));
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
	}

	public ResponseEntity<String> checkAccess(@RequestBody CedarRequest parsedRequest) {
		AuthorizationRequest request = new AuthorizationRequest(parsedRequest.getPrincipal(), parsedRequest.getAction(),
				parsedRequest.getResource(), parsedRequest.getContext(), Optional.ofNullable(schema),
				parsedRequest.isValidateRequest());

		try {
			AuthorizationResponse response = engine.isAuthorized(request, this.policySet, this.entities);

			// Prints Cedar decision to console.
			System.out.println(response.toString());

			if (response.type == AuthorizationResponse.SuccessOrFailure.Success) {
				if (response.success.toString().contains("Allow")) {
					return ResponseEntity.ok("Access Granted.");
				}
				else if (response.success.toString().contains("Deny")) {
					return ResponseEntity.status(403).body("Access Denied.");
				}
				else {
					throw new Exception(response.errors.toString());
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
