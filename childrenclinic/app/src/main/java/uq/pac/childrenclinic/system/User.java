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

package uq.pac.childrenclinic.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uq.pac.childrenclinic.model.BaseEntity;

@Entity
@Table(name = "users")
@PrimaryKeyJoinColumn(name = "entity_id")
public class User extends BaseEntity {

	@Column(name = "username")
	@NotBlank
	private String username;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	@NotEmpty
	private Set<Role> roles;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "level_id")
	private Level level;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_manager", joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "manager_id"))
	private Set<User> managers = new HashSet<>();

	@ManyToMany(mappedBy = "managers", fetch = FetchType.LAZY)
	private Set<User> subordinates = new HashSet<>();

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public void addRole(Role role) {
		this.roles.add(role);
	}

	public void removeRole(Role role) {
		this.roles.remove(role);
	}

	public String getRoleDescriptions() {
		if (this.roles == null || this.roles.isEmpty()) {
			return "No additional details.";
		}
		return this.roles.stream().map(Role::getName).sorted().collect(Collectors.joining(", "));
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public String getLevelDescriptions() {
		if (this.level == null) {
			return "No additional details.";
		}
		return this.level.getName();
	}

	public Set<User> getManagers() {
		return managers;
	}

	public void setManagers(Set<User> managers) {
		this.managers = managers;
	}

	public void addManager(User manager) {
		this.managers.add(manager);
		manager.getSubordinates().add(this);
	}

	public void removeManager(User manager) {
		this.managers.remove(manager);
		manager.getSubordinates().remove(this);
	}

	public Set<User> getSubordinates() {
		return subordinates;
	}

	public void setSubordinates(Set<User> subordinates) {
		this.subordinates = subordinates;
	}

	public void addSubordinate(User subordinate) {
		this.subordinates.add(subordinate);
		subordinate.getManagers().add(this);
	}

	public void removeSubordinate(User subordinate) {
		this.subordinates.remove(subordinate);
		subordinate.getManagers().remove(this);
	}

}
