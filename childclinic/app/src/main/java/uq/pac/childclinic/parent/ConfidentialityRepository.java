package uq.pac.childclinic.parent;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConfidentialityRepository extends JpaRepository<Confidentiality, Integer> {
    @Query("SELECT c FROM Confidentiality c ORDER BY c.name")
	List<Confidentiality> findConfidentialities();
}
