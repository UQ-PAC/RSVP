/*
 * Copyright 2012-2025 the original author or authors.
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
 */
package org.springframework.samples.petclinic.vet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.samples.petclinic.cedar.CedarAuthorization;
import org.springframework.samples.petclinic.cedar.CedarRequest;
import org.springframework.samples.petclinic.cedar.CedarService;

import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.Value;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	private final VetRepository vetRepository;

	private final CedarService cedarService;

	public VetController(VetRepository vetRepository, CedarService cedarService) {
		this.vetRepository = vetRepository;
		this.cedarService = cedarService;
	}

	@GetMapping("/vets.html")
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String showVetList(@RequestParam(defaultValue = "1") int page, Model model, HttpSession session) {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for Object-Xml mapping
		Vets vets = new Vets();
		Page<Vet> paginated = findPaginated(page);

		String principalId = (String) session.getAttribute("currentUser");
		if (session.getAttribute("currentUser") == null) {
			principalId = "Guest";
		}

		System.out.println("Cookie principalId: " + principalId);

		EntityUID principal;
		if (principalId.equals("Guest")) {
			principal = EntityUID.parse("PetClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			principal = EntityUID.parse("PetClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}

		List<Vet> authorizedVets = paginated.stream().filter(vet -> {
			EntityUID action = EntityUID.parse("PetClinic::Action::\"" + "ViewEmployee" + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
			EntityUID resource = EntityUID
				.parse("PetClinic::Employee::\"" + vet.getFirstName() + " " + vet.getLastName() + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));
			Map<String, Value> contextMap = new HashMap<>();
			CedarRequest cedarReq = new CedarRequest(principal, action, resource, contextMap, true);
			ResponseEntity<String> response = cedarService.checkAccess(cedarReq);
			if (response.getBody().startsWith("Access Granted.")) {
				return true;
			}
			else {
				return false;
			}
		}).collect(Collectors.toList());

		vets.getVetList().addAll(authorizedVets);

		Page<Vet> filteredPaginated = new PageImpl<>(authorizedVets, PageRequest.of(page - 1, 5),
				authorizedVets.size());

		return addPaginationModel(page, filteredPaginated, model);
	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vetRepository.findAll(pageable);
	}

	@GetMapping({ "/vets" })
	@CedarAuthorization(action = "ListEmployees", resourceType = "Clinic", resourceId = "Any", validate = true)
	public @ResponseBody Vets showResourcesVetList(HttpSession session) {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for JSon/Object mapping
		Vets vets = new Vets();

		String principalId = (String) session.getAttribute("currentUser");
		EntityUID principal;
		if (principalId.equals("Guest")) {
			principal = EntityUID.parse("PetClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			principal = EntityUID.parse("PetClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}

		List<Vet> authorizedVets = this.vetRepository.findAll().stream().filter(vet -> {
			EntityUID action = EntityUID.parse("PetClinic::Action::\"" + "ViewEmployee" + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
			EntityUID resource = EntityUID
				.parse("PetClinic::Employee::\"" + vet.getFirstName() + " " + vet.getLastName() + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));
			Map<String, Value> contextMap = new HashMap<>();
			CedarRequest cedarReq = new CedarRequest(principal, action, resource, contextMap, true);
			ResponseEntity<String> response = cedarService.checkAccess(cedarReq);
			if (response.getBody().startsWith("Access Granted.")) {
				return true;
			}
			else {
				return false;
			}
		}).collect(Collectors.toList());

		vets.getVetList().addAll(authorizedVets);
		return vets;
	}

}
