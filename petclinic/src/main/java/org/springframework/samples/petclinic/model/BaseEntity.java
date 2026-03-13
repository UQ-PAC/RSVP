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
package org.springframework.samples.petclinic.model;

import java.io.Serializable;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple JavaBean domain object with an id property. Used as a base class for objects
 * needing this property.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
@Entity
@Table(name = "entities")
@Inheritance(strategy = InheritanceType.JOINED)
public class BaseEntity implements Serializable, Identifiable {

	@Id
	@Column(name = "entity_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_sequence_generator")
	@SequenceGenerator(name = "entity_sequence_generator", sequenceName = "entities_sequence", allocationSize = 1)
	private Integer id;

	@ManyToMany
	@JoinTable(name = "entity_databases", joinColumns = @JoinColumn(name = "entity_id"),
			inverseJoinColumns = @JoinColumn(name = "database_id"))
	private Set<Database> databases = new HashSet<>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isNew() {
		return this.id == null;
	}

	public Set<Database> getDatabases() {
		return databases;
	}

	public void setDatabases(Set<Database> databases) {
		this.databases = databases;
	}

	public void addDatabase(Database database) {
		this.databases.add(database);
	}

}
