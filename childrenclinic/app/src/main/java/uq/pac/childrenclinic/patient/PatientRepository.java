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

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {

	@EntityGraph(attributePaths = { "clinics", "gender", "doctors", "doctors.specialties", "guardians",
			"guardians.guardian", "guardians.authority", "visits", "visits.confidentiality", "visits.clinics",
			"visits.doctors", "visits.doctors.specialties", "visits.guardians" })
	@Transactional(readOnly = true)
	Page<Patient> findByLastNameStartingWith(String lastName, Pageable pageable);

	@EntityGraph(attributePaths = { "clinics", "gender", "doctors", "doctors.specialties", "guardians",
			"guardians.guardian", "guardians.authority", "visits", "visits.confidentiality", "visits.clinics",
			"visits.doctors", "visits.doctors.specialties", "visits.guardians" })
	@Transactional(readOnly = true)
	Optional<Patient> findById(Integer id);

}
