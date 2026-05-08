package uq.pac.childrenclinic.visit;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConfidentialityRepository extends JpaRepository<Confidentiality, Integer> {

	@Query("SELECT c FROM Confidentiality c ORDER BY c.name")
	@Cacheable("confidentialities")
	List<Confidentiality> findConfidentialities();

}
