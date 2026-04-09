package org.springframework.samples.petclinic.system;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements Serializable {

	@Id
	private Integer entity_id;

	private String username;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "entity_clinics", joinColumns = @JoinColumn(name = "entity_id"),
			inverseJoinColumns = @JoinColumn(name = "clinic_id"))
	private Set<Clinic> clinics;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;

	public Integer getId() {
		return entity_id;
	}

	public void setId(Integer entity_id) {
		this.entity_id = entity_id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<Clinic> getClinics() {
		return clinics;
	}

	public void setClinics(Set<Clinic> clinics) {
		this.clinics = clinics;
	}

	public String getClinicDescriptions() {
		if (this.clinics == null || this.clinics.isEmpty()) {
			return "No Additional Details.";
		}
		return this.clinics.stream().map(Clinic::getClinicName).sorted().collect(Collectors.joining(", "));
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getRoleDescriptions() {
		if (this.roles == null || this.roles.isEmpty()) {
			return "No Additional Details.";
		}
		return this.roles.stream().map(Role::getName).sorted().collect(Collectors.joining(", "));
	}

}
