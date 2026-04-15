package uq.pac.childclinic.system;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClinicRepository extends JpaRepository<Clinic, Integer> {
    @Query("SELECT c FROM Clinic c ORDER BY c.name")
	List<Clinic> findClinics();
}
