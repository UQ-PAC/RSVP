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

package uq.pac.childrenclinic.patient;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.util.Objects;

import uq.pac.childrenclinic.guardian.Guardian;

@Entity
@Table(name = "patient_guardians")
public class PatientGuardian {

	@EmbeddedId
	private PatientGuardianId id = new PatientGuardianId();

	@ManyToOne
	@MapsId("patientId")
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@ManyToOne
	@MapsId("guardianId")
	@JoinColumn(name = "guardian_id")
	private Guardian guardian;

	@ManyToOne
	@JoinColumn(name = "authority_id")
	private GuardianAuthority authority;

	public PatientGuardian() {
	}

	public PatientGuardian(Patient patient, Guardian guardian, GuardianAuthority authority) {
		this.patient = patient;
		this.guardian = guardian;
		this.authority = authority;
		if (patient != null && patient.getId() != null && guardian != null && guardian.getId() != null) {
			this.id = new PatientGuardianId(patient.getId(), guardian.getId());
		}
	}

	public PatientGuardianId getId() {
		return id;
	}

	public void setId(PatientGuardianId id) {
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

	public Guardian getGuardian() {
		return guardian;
	}

	public void setGuardian(Guardian guardian) {
		this.guardian = guardian;
		if (guardian != null) {
			this.id.setGuardianId(guardian.getId());
		}
	}

	public GuardianAuthority getAuthority() {
		return authority;
	}

	public void setAuthority(GuardianAuthority authority) {
		this.authority = authority;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PatientGuardian that = (PatientGuardian) o;
		return id != null && id.getPatientId() != null && id.getGuardianId() != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

}
