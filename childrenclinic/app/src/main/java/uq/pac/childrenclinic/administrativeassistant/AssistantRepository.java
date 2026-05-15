package uq.pac.childrenclinic.administrativeassistant;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface AssistantRepository extends JpaRepository<Assistant, Integer> {

	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Page<Assistant> findByLastNameStartingWith(String lastName, Pageable pageable);

	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Optional<Assistant> findById(Integer id);

}
