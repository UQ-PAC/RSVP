package uq.pac.childrenclinic.doctor;

import org.springframework.data.jpa.repository.JpaRepository;

import uq.pac.childclinic.doctor.Specialty;

public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {

}
