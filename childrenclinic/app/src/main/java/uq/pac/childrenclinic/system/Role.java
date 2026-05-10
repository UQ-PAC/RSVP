package uq.pac.childrenclinic.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Objects;

import uq.pac.childrenclinic.model.Identifiable;

@Entity
@Table(name = "roles")
public class Role implements Serializable, Identifiable {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name")
	@NotBlank
	private String name;

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Role role = (Role) o;
		return id != null && Objects.equals(this.id, role.getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
