package uq.pac.childrenclinic.patient;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {

	@EntityGraph(attributePaths = { "clinics", "gender", "doctors", "doctors.specialties", "guardians",
			"guardians.guardian", "guardians.authority", "visits", "visits.confidentiality",
			"visits.clinics", "visits.doctors", "visits.doctors.specialties", "visits.guardians" })
	@Transactional(readOnly = true)
	Page<Patient> findByLastNameStartingWith(String lastName, Pageable pageable);

	@EntityGraph(attributePaths = { "clinics", "gender", "doctors", "doctors.specialties", "guardians",
			"guardians.guardian", "guardians.authority", "visits", "visits.confidentiality",
			"visits.clinics", "visits.doctors", "visits.doctors.specialties", "visits.guardians" })
	@Transactional(readOnly = true)
	Optional<Patient> findById(Integer id);

}
