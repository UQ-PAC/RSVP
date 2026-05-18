package uq.pac.childrenclinic.receptionist;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ReceptionistRepository extends JpaRepository<Receptionist, Integer> {

	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Page<Receptionist> findByLastNameStartingWith(String lastName, Pageable pageable);

	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Optional<Receptionist> findById(Integer id);

}
