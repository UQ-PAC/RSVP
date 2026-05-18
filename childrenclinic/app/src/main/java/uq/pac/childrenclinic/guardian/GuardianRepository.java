package uq.pac.childrenclinic.guardian;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface GuardianRepository extends JpaRepository<Guardian, Integer> {

	/**
	 * Retrieves paginated Guardian entities matching a specified last name prefix. * @param
	 * lastName The prefix of the last name to query.
	 * @param pageable The pagination execution parameters.
	 * @return A paginated subset of matching Guardian entities.
	 */
	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Page<Guardian> findByLastNameStartingWith(String lastName, Pageable pageable);

	/**
	 * Retrieves an Guardian entity by its primary key identifier. * @param id The primary
	 * key to search for.
	 * @return An Optional containing the Guardian if present.
	 */
	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Optional<Guardian> findById(Integer id);

}
