/*
 * Copyright 2012-2025 the original author or authors.
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
package org.springframework.samples.petclinic.owner;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

import org.springframework.samples.petclinic.model.Person;

/**
 * Simple JavaBean domain object representing a parent.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Oliver Drotbohm
 * @author Wick Dynex
 */
@Entity
@Table(name = "parents")
@PrimaryKeyJoinColumn(name = "entity_id")
public class Owner extends Person {

	@Column(name = "address")
	@NotBlank
	private String address;

	@Column(name = "city")
	@NotBlank
	private String city;

	@Column(name = "telephone")
	@NotBlank
	@Pattern(regexp = "\\d{10}", message = "{telephone.invalid}")
	private String telephone;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_id")
	@OrderBy("name")
	private final List<Child> children = new ArrayList<>();

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getTelephone() {
		return this.telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public List<Child> getChildren() {
		return this.children;
	}

	public void addChild(Child child) {
		if (child.isNew()) {
			getChildren().add(child);
		}
	}

	/**
	 * Return the Child with the given name, or null if none found for this Parent.
	 * @param name to test
	 * @return the Child with the given name, or null if no such Child exists for this Parent
	 */
	public Child getChild(String name) {
		return getChild(name, false);
	}

	/**
	 * Return the Child with the given id, or null if none found for this Parent.
	 * @param id to test
	 * @return the Child with the given id, or null if no such Child exists for this Parent
	 */
	public Child getChild(Integer id) {
		for (Child child : getChildren()) {
			if (!child.isNew()) {
				Integer compId = child.getId();
				if (Objects.equals(compId, id)) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * Return the Child with the given name, or null if none found for this Parent.
	 * @param name to test
	 * @param ignoreNew whether to ignore new children (children that are not saved yet)
	 * @return the Child with the given name, or null if no such Child exists for this Parent
	 */
	public Child getChild(String name, boolean ignoreNew) {
		for (Child child : getChildren()) {
			String compName = child.getName();
			if (compName != null && compName.equalsIgnoreCase(name)) {
				if (!ignoreNew || !child.isNew()) {
					return child;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("new", this.isNew())
			.append("lastName", this.getLastName())
			.append("firstName", this.getFirstName())
			.append("address", this.address)
			.append("city", this.city)
			.append("telephone", this.telephone)
			.toString();
	}

	/**
	 * Adds the given {@link Visit} to the {@link Child} with the given identifier.
	 * @param childId the identifier of the {@link Child}, must not be {@literal null}.
	 * @param visit the visit to add, must not be {@literal null}.
	 */
	public void addVisit(Integer childId, Visit visit) {

		Assert.notNull(childId, "Child identifier must not be null!");
		Assert.notNull(visit, "Visit must not be null!");

		Child child = getChild(childId);

		Assert.notNull(child, "Invalid Child identifier!");

		child.addVisit(visit);
	}

}
