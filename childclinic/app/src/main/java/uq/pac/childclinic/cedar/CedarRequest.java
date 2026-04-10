package uq.pac.childclinic.cedar;

import com.cedarpolicy.model.Context;
import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class CedarRequest {

	@JsonProperty("principal")
	private final EntityUID principal;

	@JsonProperty("action")
	private final EntityUID action;

	@JsonProperty("resource")
	private final EntityUID resource;

	@JsonProperty("context")
	private final Map<String, Value> context;

	@JsonProperty("validateRequest")
	private final boolean validateRequest;

	public CedarRequest(EntityUID principal, EntityUID action, EntityUID resource, Map<String, Value> context,
			boolean validateRequest) {
		this.principal = principal;
		this.action = action;
		this.resource = resource;
		this.context = context;
		this.validateRequest = validateRequest;
	}

	public EntityUID getPrincipal() {
		return principal;
	}

	public EntityUID getAction() {
		return action;
	}

	public EntityUID getResource() {
		return resource;
	}

	public Context getContext() {
		if (this.context == null) {
			return new Context(new HashMap<>());
		}
		return new Context(this.context);
	}

	public boolean isValidateRequest() {
		return validateRequest;
	}

}
