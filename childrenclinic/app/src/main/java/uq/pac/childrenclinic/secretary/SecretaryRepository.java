package uq.pac.childrenclinic.secretary;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SecretaryRepository extends JpaRepository<Secretary, Integer> {

	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Page<Secretary> findByLastNameStartingWith(String lastName, Pageable pageable);

	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Optional<Secretary> findById(Integer id);

}
