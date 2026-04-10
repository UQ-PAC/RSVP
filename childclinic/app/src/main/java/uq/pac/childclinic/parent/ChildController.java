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

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uq.pac.childclinic.cedar.CedarAuthorization;
import uq.pac.childclinic.cedar.CedarRequest;
import uq.pac.childclinic.cedar.CedarService;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Wick Dynex
 */
@Controller
@RequestMapping("/parents/{parentId}")
class ChildController {

	private static final String VIEWS_CHILDREN_CREATE_OR_UPDATE_FORM = "children/createOrUpdateChildForm";

	private final ParentRepository parents;

	private final GenderRepository genders;

	private final CedarService cedarService;

	public ChildController(ParentRepository parents, GenderRepository genders, CedarService cedarService) {
		this.parents = parents;
		this.genders = genders;
		this.cedarService = cedarService;
	}

	@ModelAttribute("genders")
	public Collection<Gender> populateGenders() {
		return this.genders.findGenders();
	}

	@ModelAttribute("parent")
	public Parent findParent(@PathVariable("parentId") int parentId) {
		Optional<Parent> optionalParent = this.parents.findById(parentId);
		Parent parent = optionalParent.orElseThrow(() -> new IllegalArgumentException(
				"Parent not found with id: " + parentId + ". Please ensure the ID is correct."));
		return parent;
	}

	@ModelAttribute("child")
	public Child findChild(@PathVariable("parentId") int parentId,
			@PathVariable(name = "childId", required = false) Integer childId) {

		if (childId == null) {
			return new Child();
		}

		Optional<Parent> optionalParent = this.parents.findById(parentId);
		Parent parent = optionalParent.orElseThrow(() -> new IllegalArgumentException(
				"Parent not found with id: " + parentId + ". Please ensure the ID is correct."));
		return parent.getChild(childId);
	}

	@InitBinder("parent")
	public void initParentBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("child")
	public void initChildBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new ChildValidator());
	}

	@GetMapping("/children/new")
	@CedarAuthorization(action = "AddClient", resourceType = "Parent", validate = true)
	public String initCreationForm(Parent parent, ModelMap model, HttpSession session) {
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

		EntityUID action = EntityUID.parse("ChildClinic::Action::\"" + "EditClient" + "\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
		EntityUID resource = EntityUID
			.parse("ChildClinic::Parent::\"" + parent.getFirstName() + " " + parent.getLastName() + "\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));
		Map<String, Value> contextMap = new HashMap<>();
		CedarRequest cedarReq = new CedarRequest(principal, action, resource, contextMap, true);
		ResponseEntity<String> response = cedarService.checkAccess(cedarReq);
		if (!response.getBody().startsWith("Access Granted.")) {
			throw new SecurityException("Access Denied to modify Parent.");
		}

		Child child = new Child();
		parent.addChild(child);
		return VIEWS_CHILDREN_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/children/new")
	@CedarAuthorization(action = "AddClient", resourceType = "Parent", validate = true)
	public String processCreationForm(Parent parent, @Valid Child child, BindingResult result,
			RedirectAttributes redirectAttributes) {

		if (StringUtils.hasText(child.getName()) && child.isNew() && parent.getChild(child.getName(), true) != null)
			result.rejectValue("name", "duplicate", "already exists");

		LocalDate currentDate = LocalDate.now();
		if (child.getBirthDate() != null && child.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		if (result.hasErrors()) {
			return VIEWS_CHILDREN_CREATE_OR_UPDATE_FORM;
		}

		parent.addChild(child);
		this.parents.save(parent);
		redirectAttributes.addFlashAttribute("message", "New Child has been Added.");
		return "redirect:/parents/{parentId}";
	}

	@GetMapping("/children/{childId}/edit")
	@CedarAuthorization(action = "EditClient", resourceType = "Child", validate = true)
	public String initUpdateForm() {
		return VIEWS_CHILDREN_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/children/{childId}/edit")
	@CedarAuthorization(action = "EditClient", resourceType = "Child", validate = true)
	public String processUpdateForm(Parent parent, @Valid Child child, BindingResult result,
			RedirectAttributes redirectAttributes) {

		String childName = child.getName();

		// checking if the child name already exists for the parent
		if (StringUtils.hasText(childName)) {
			Child existingChild = parent.getChild(childName, false);
			if (existingChild != null && !Objects.equals(existingChild.getId(), child.getId())) {
				result.rejectValue("name", "duplicate", "already exists");
			}
		}

		LocalDate currentDate = LocalDate.now();
		if (child.getBirthDate() != null && child.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		if (result.hasErrors()) {
			return VIEWS_CHILDREN_CREATE_OR_UPDATE_FORM;
		}

		updateChildDetails(parent, child);
		redirectAttributes.addFlashAttribute("message", "Child details have been edited.");
		return "redirect:/parents/{parentId}";
	}

	/**
	 * Updates the child details if it exists or adds a new child to the parent.
	 * @param parent The parent of the child
	 * @param child The child with updated details
	 */
	private void updateChildDetails(Parent parent, Child child) {
		Integer id = child.getId();
		Assert.state(id != null, "'child.getId()' must not be null");
		Child existingChild = parent.getChild(id);
		if (existingChild != null) {
			// Update existing child's properties
			existingChild.setName(child.getName());
			existingChild.setBirthDate(child.getBirthDate());
			existingChild.setGender(child.getGender());
		}
		else {
			parent.addChild(child);
		}
		this.parents.save(parent);
	}

}
