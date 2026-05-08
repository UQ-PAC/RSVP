package uq.pac.childrenclinic.patient;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdultAuthorityRepository extends JpaRepository<AdultAuthority, Integer> {

    @Override
    @Cacheable("authorities")
    List<AdultAuthority> findAll();

}
