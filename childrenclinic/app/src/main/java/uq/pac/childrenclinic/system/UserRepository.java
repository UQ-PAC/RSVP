package uq.pac.childrenclinic.system;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {

	Optional<User> findByUsername(String username);

}
