package uq.pac.childrenclinic.doctor;

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
@Table(name = "specialties")
public class Specialty implements Serializable, Identifiable {

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
		return this.name;
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
		Specialty specialty = (Specialty) o;
		return id != null && Objects.equals(this.id, specialty.getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
