package uq.pac.childrenclinic.patient;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PatientGuardianId implements Serializable {

	@Column(name = "patient_id")
	private Integer patientId;

	@Column(name = "guardian_id")
	private Integer guardianId;

	public PatientGuardianId() {
	}

	public PatientGuardianId(Integer patientId, Integer guardianId) {
		this.patientId = patientId;
		this.guardianId = guardianId;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public Integer getGuardianId() {
		return guardianId;
	}

	public void setGuardianId(Integer guardianId) {
		this.guardianId = guardianId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PatientGuardianId that = (PatientGuardianId) o;
		return patientId != null && guardianId != null && Objects.equals(patientId, that.patientId)
				&& Objects.equals(guardianId, that.guardianId);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
