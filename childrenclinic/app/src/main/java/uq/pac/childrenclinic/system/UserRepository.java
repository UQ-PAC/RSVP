package uq.pac.childrenclinic.system;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Integer> {

	Optional<User> findByUsername(String username);

	@Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
	List<User> findByRoleName(@Param("roleName") String roleName);

}
