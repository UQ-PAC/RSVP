package uq.pac.childrenclinic.patient;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.util.Objects;

import uq.pac.childrenclinic.adult.Adult;

@Entity
@Table(name = "patient_adults")
public class PatientAdult {

	@EmbeddedId
	private PatientAdultId id = new PatientAdultId();

	@ManyToOne
	@MapsId("patientId")
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@ManyToOne
	@MapsId("adultId")
	@JoinColumn(name = "adult_id")
	private Adult adult;

	@ManyToOne
	@JoinColumn(name = "authority_id")
	private AdultAuthority authority;

	public PatientAdult() {
	}

	public PatientAdult(Patient patient, Adult adult, AdultAuthority authority) {
		this.patient = patient;
		this.adult = adult;
		this.authority = authority;
		if (patient != null && patient.getId() != null && adult != null && adult.getId() != null) {
			this.id = new PatientAdultId(patient.getId(), adult.getId());
		}
	}

	public PatientAdultId getId() {
		return id;
	}

	public void setId(PatientAdultId id) {
		this.id = id;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
		if (patient != null) {
			this.id.setPatientId(patient.getId());
		}
	}

	public Adult getAdult() {
		return adult;
	}

	public void setAdult(Adult adult) {
		this.adult = adult;
		if (adult != null) {
			this.id.setAdultId(adult.getId());
		}
	}

	public AdultAuthority getAuthority() {
		return authority;
	}

	public void setAuthority(AdultAuthority authority) {
		this.authority = authority;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PatientAdult that = (PatientAdult) o;
		return id != null && id.getPatientId() != null && id.getAdultId() != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
