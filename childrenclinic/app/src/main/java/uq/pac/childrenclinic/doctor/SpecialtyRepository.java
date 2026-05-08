package uq.pac.childrenclinic.doctor;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {

    @Override
    @Cacheable("specialties")
    List<Specialty> findAll();

}
