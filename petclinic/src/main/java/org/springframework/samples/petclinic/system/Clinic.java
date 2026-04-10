package org.springframework.samples.petclinic.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "clinics")
public class Clinic implements Serializable {

	@Id
	@Column(name = "clinic_id")
	private Integer database_id;

	@Column(name = "name")
	private String name;

	public Integer getClinicId() {
		return database_id;
	}

	public void setClinicId(Integer database_id) {
		this.database_id = database_id;
	}

	public String getClinicName() {
		return name;
	}

	public void setClinicName(String name) {
		this.name = name;
	}

}
