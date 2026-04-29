package uq.pac.childrenclinic.system;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import uq.pac.childclinic.system.User;

public interface UserRepository extends CrudRepository<User, Integer> {

	Optional<User> findByUsername(String username);

}
