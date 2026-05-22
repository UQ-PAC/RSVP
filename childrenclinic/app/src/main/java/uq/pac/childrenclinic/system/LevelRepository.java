package uq.pac.childrenclinic.system;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LevelRepository extends JpaRepository<Level, Integer> {

	@Query("SELECT l FROM Level l ORDER BY l.name")
	@Cacheable("levels")
	List<Level> findLevels();

	Optional<Level> findByName(String name);

}
