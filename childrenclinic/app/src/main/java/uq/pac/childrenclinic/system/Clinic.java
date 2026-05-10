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
@Table(name = "clinics")
public class Clinic implements Serializable, Identifiable {

	@Id
	@Column(name = "clinic_id")
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

	public String getClinicName() {
		return name;
	}

	public void setClinicName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Clinic clinic = (Clinic) o;
		return id != null && Objects.equals(this.id, clinic.getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
