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

package uq.pac.childrenclinic.guardian;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface GuardianRepository extends JpaRepository<Guardian, Integer> {

	/**
	 * Retrieves paginated Guardian entities matching a specified last name prefix.
	 * * @param lastName The prefix of the last name to query.
	 * @param pageable The pagination execution parameters.
	 * @return A paginated subset of matching Guardian entities.
	 */
	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Page<Guardian> findByLastNameStartingWith(String lastName, Pageable pageable);

	/**
	 * Retrieves an Guardian entity by its primary key identifier. * @param id The primary
	 * key to search for.
	 * @return An Optional containing the Guardian if present.
	 */
	@EntityGraph(attributePaths = { "clinics", "gender" })
	@Transactional(readOnly = true)
	Optional<Guardian> findById(Integer id);

}
