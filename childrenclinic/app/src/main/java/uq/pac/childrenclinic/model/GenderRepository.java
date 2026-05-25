/*
 * Copyright 2012-2025 the original author or authors.
 * Copyright 2026 Gabriel Henrique Lopes Gomes Alves Nunes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file has been modified from the original Spring PetClinic project
 * (https://github.com/spring-projects/spring-petclinic).
 */

package uq.pac.childrenclinic.model;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository class for <code>Gender</code> domain objects.
 *
 * @author Patrick Baumgartner
 */

public interface GenderRepository extends JpaRepository<Gender, Integer> {

	/**
	 * Retrieve all {@link Gender}s from the data store.
	 * @return a Collection of {@link Gender}s.
	 */
	@Query("SELECT g FROM Gender g ORDER BY g.name")
	@Cacheable("genders")
	List<Gender> findGenders();

}
