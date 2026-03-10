package org.springframework.samples.petclinic.cedar;

import com.cedarpolicy.model.Context;
import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.Value;

import java.util.HashMap;
import java.util.Map;

public class CedarRequest {

	private EntityUID principal;

	private EntityUID action;

	private EntityUID resource;

	private Map<String, Value> context;

	private boolean validateRequest;

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

	public void setPrincipal(EntityUID principal) {
		this.principal = principal;
	}

	public EntityUID getAction() {
		return action;
	}

	public void setAction(EntityUID action) {
		this.action = action;
	}

	public EntityUID getResource() {
		return resource;
	}

	public void setResource(EntityUID resource) {
		this.resource = resource;
	}

	public Context getContext() {
		if (this.context == null) {
			return new Context(new HashMap<>());
		}
		return new Context(this.context);
	}

	public void setContext(Map<String, Value> context) {
		this.context = context;
	}

	public boolean isValidateRequest() {
		return validateRequest;
	}

	public void setValidateRequest(boolean validateRequest) {
		this.validateRequest = validateRequest;
	}

}
