package uq.pac.childclinic.adult;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdultRepository extends JpaRepository<Adult, Integer> {

	/**
	 * Retrieves paginated Adult entities matching a specified last name prefix. * @param
	 * lastName The prefix of the last name to query.
	 * @param pageable The pagination execution parameters.
	 * @return A paginated subset of matching Adult entities.
	 */
	Page<Adult> findByLastNameStartingWith(String lastName, Pageable pageable);

	/**
	 * Retrieves an Adult entity by its primary key identifier. * @param id The primary
	 * key to search for.
	 * @return An Optional containing the Adult if present.
	 */
	Optional<Adult> findById(Integer id);

}
