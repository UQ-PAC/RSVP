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

	@Override
	public String toString() {
		return "{principal = " + this.getPrincipal() + ", action = " + this.getAction() + ", resource = "
				+ this.getResource() + ", context = " + this.getContext() + ", validateRequest = "
				+ this.isValidateRequest() + "}";
	}

}
