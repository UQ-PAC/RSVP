package uq.pac.childrenclinic.system;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClinicRepository extends JpaRepository<Clinic, Integer> {

	@Query("SELECT c FROM Clinic c ORDER BY c.name")
	@Cacheable("clinics")
	List<Clinic> findClinics();

	Optional<Clinic> findByName(String name);

}
