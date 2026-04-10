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
package uq.pac.childclinic.parent;

import com.cedarpolicy.value.EntityUID;
import com.cedarpolicy.value.Value;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uq.pac.childclinic.cedar.CedarAuthorization;
import uq.pac.childclinic.cedar.CedarRequest;
import uq.pac.childclinic.cedar.CedarService;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Controller
class ParentController {

	private static final String VIEWS_PARENT_CREATE_OR_UPDATE_FORM = "parents/createOrUpdateParentForm";

	private final ParentRepository parents;

	private final CedarService cedarService;

	public ParentController(ParentRepository parents, CedarService cedarService) {
		this.parents = parents;
		this.cedarService = cedarService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("parent")
	public Parent findParent(@PathVariable(name = "parentId", required = false) Integer parentId) {
		return parentId == null ? new Parent()
				: this.parents.findById(parentId)
					.orElseThrow(() -> new IllegalArgumentException("Parent not found with id: " + parentId
							+ ". Please ensure the ID is correct and the parent exists in the database."));
	}

	@GetMapping("/parents/new")
	@CedarAuthorization(action = "AddClient", resourceType = "Clinic", validate = true)
	public String initCreationForm() {
		return VIEWS_PARENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/parents/new")
	@CedarAuthorization(action = "AddClient", resourceType = "Clinic", validate = true)
	public String processCreationForm(@Valid Parent parent, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the parent.");
			return VIEWS_PARENT_CREATE_OR_UPDATE_FORM;
		}

		this.parents.save(parent);
		redirectAttributes.addFlashAttribute("message", "New Parent Created.");
		return "redirect:/parents/" + parent.getId();
	}

	@GetMapping("/parents/find")
	@CedarAuthorization(action = "ListClients", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String initFindForm() {
		return "parents/findParents";
	}

	@GetMapping("/parents")
	@CedarAuthorization(action = "ListClients", resourceType = "Clinic", resourceId = "Any", validate = true)
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Parent parent, BindingResult result,
			Model model, HttpSession session) {
		// allow parameterless GET request for /parents to return all records
		String lastName = parent.getLastName();
		if (lastName == null) {
			lastName = ""; // empty string signifies broadest possible search
		}

		String principalId = (String) session.getAttribute("currentUser");
		if (session.getAttribute("currentUser") == null) {
			principalId = "Guest";
		}

		System.out.println("Cookie principalId: " + principalId);

		EntityUID principal;
		if (principalId.equals("Guest")) {
			principal = EntityUID.parse("ChildClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			principal = EntityUID.parse("ChildClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}

		// find parents by last name
		Page<Parent> parentsResults = findPaginatedForParentsLastName(page, lastName);

		List<Parent> authorizedParents = parentsResults.stream().filter(o -> {
			EntityUID action = EntityUID.parse("ChildClinic::Action::\"" + "ViewClient" + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
			EntityUID resource = EntityUID
				.parse("ChildClinic::Parent::\"" + o.getFirstName() + " " + o.getLastName() + "\"")
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

		Map<Integer, String> parentAuthorizationMap = parentsResults.stream()
			.collect(Collectors.toMap(Parent::getId, o -> {
				EntityUID action = EntityUID.parse("ChildClinic::Action::\"" + "ViewClient" + "\"")
					.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
				EntityUID resource = EntityUID
					.parse("ChildClinic::Parent::\"" + o.getFirstName() + " " + o.getLastName() + "\"")
					.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));
				Map<String, Value> contextMap = new HashMap<>();
				CedarRequest cedarReq = new CedarRequest(principal, action, resource, contextMap, true);
				ResponseEntity<String> response = cedarService.checkAccess(cedarReq);
				return response.getBody();
			}));

		if (authorizedParents.isEmpty()) {
			// no parents found
			result.rejectValue("lastName", "notFound", "No parents found.");
			return "parents/findParents";
		}

		if (authorizedParents.size() == 1 && parentsResults.getTotalElements() == 1) {
			// 1 parent found
			parent = authorizedParents.iterator().next();
			if (parentAuthorizationMap.get(parent.getId()).startsWith("Access Granted.")) {
				return "redirect:/parents/" + parent.getId();
			}
		}

		model.addAttribute("authMap", parentAuthorizationMap);

		return addPaginationModel(page, model, parentsResults);
	}

	private String addPaginationModel(int page, Model model, Page<Parent> paginated) {
		List<Parent> listParents = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listParents", listParents);
		return "parents/parentsList";
	}

	private Page<Parent> findPaginatedForParentsLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return parents.findByLastNameStartingWith(lastname, pageable);
	}

	@GetMapping("/parents/{parentId}/edit")
	@CedarAuthorization(action = "EditClient", resourceType = "Parent", validate = true)
	public String initUpdateParentForm() {
		return VIEWS_PARENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/parents/{parentId}/edit")
	@CedarAuthorization(action = "EditClient", resourceType = "Parent", validate = true)
	public String processUpdateParentForm(@Valid Parent parent, BindingResult result, @PathVariable("parentId") int parentId,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the parent.");
			return VIEWS_PARENT_CREATE_OR_UPDATE_FORM;
		}

		if (!Objects.equals(parent.getId(), parentId)) {
			result.rejectValue("id", "mismatch", "The parent ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Parent ID mismatch. Please try again.");
			return "redirect:/parents/{parentId}/edit";
		}

		parent.setId(parentId);
		this.parents.save(parent);
		redirectAttributes.addFlashAttribute("message", "Parent Values Updated.");
		return "redirect:/parents/{parentId}";
	}

	/**
	 * Custom handler for displaying an parent.
	 * @param parentId the ID of the parent to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/parents/{parentId}")
	@CedarAuthorization(action = "ViewClient", resourceType = "Parent", validate = true)
	public ModelAndView showParent(@PathVariable("parentId") int parentId) {
		ModelAndView mav = new ModelAndView("parents/parentDetails");
		Optional<Parent> optionalParent = this.parents.findById(parentId);
		Parent parent = optionalParent.orElseThrow(() -> new IllegalArgumentException(
				"Parent not found with id: " + parentId + ". Please ensure the ID is correct."));
		mav.addObject(parent);
		return mav;
	}

}
