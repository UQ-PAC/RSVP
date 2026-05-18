package uq.pac.childrenclinic.patient;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianAuthorityRepository extends JpaRepository<GuardianAuthority, Integer> {

	@Override
	@Cacheable("authorities")
	List<GuardianAuthority> findAll();

}
