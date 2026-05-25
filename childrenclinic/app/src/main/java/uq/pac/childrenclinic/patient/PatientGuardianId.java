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
