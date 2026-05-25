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

package uq.pac.childrenclinic.receptionist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.core.style.ToStringCreator;

import uq.pac.childrenclinic.model.Person;

@Entity
@Table(name = "receptionists")
@PrimaryKeyJoinColumn(name = "entity_id")
public class Receptionist extends Person {

	@Column(name = "telephone")
	@NotBlank
	@Pattern(regexp = "^\\+?[0-9\\-\\s]{10,20}$", message = "{telephone.invalid}")
	private String telephone;

	public String getTelephone() {
		return this.telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("new", this.isNew())
			.append("lastName", this.getLastName())
			.append("firstName", this.getFirstName())
			.append("telephone", this.getTelephone())
			.append("birthDate", this.getBirthDate())
			.append("gender", this.getGender())
			.toString();
	}

}
