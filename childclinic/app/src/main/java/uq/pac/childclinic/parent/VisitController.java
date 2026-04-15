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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uq.pac.childclinic.cedar.CedarAuthorization;
import uq.pac.childclinic.cedar.CedarRequest;
import uq.pac.childclinic.cedar.CedarService;
import uq.pac.childclinic.system.Clinic;
import uq.pac.childclinic.system.ClinicRepository;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Wick Dynex
 */
@Controller
class VisitController {

	private final ParentRepository parents;

	private final CedarService cedarService;

	private final ConfidentialityRepository confidentialities;

	private final ClinicRepository clinics;

	public VisitController(ParentRepository parents, CedarService cedarService, ConfidentialityRepository confidentialities, ClinicRepository clinics) {
		this.parents = parents;
		this.cedarService = cedarService;
		this.confidentialities = confidentialities;
		this.clinics = clinics;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("confidentialities")
	public Collection<Confidentiality> populateConfidentialities() {
		return this.confidentialities.findConfidentialities();
	}

	@ModelAttribute("clinics")
	public Collection<Clinic> populateClinics() {
		return this.clinics.findClinics();
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Child object always has an id (Even though id is not part of the form fields)
	 * @param childId
	 * @return Child
	 */
	@ModelAttribute("visit")
	public Visit loadChildWithVisit(@PathVariable("parentId") int parentId, @PathVariable("childId") int childId,
			Map<String, Object> model) {
		Optional<Parent> optionalParent = parents.findById(parentId);
		Parent parent = optionalParent.orElseThrow(() -> new IllegalArgumentException(
				"Parent not found with id: " + parentId + ". Please ensure the ID is correct."));

		Child child = parent.getChild(childId);
		if (child == null) {
			throw new IllegalArgumentException(
					"Child with id " + childId + " not found for parent with id " + parentId + ".");
		}
		model.put("child", child);
		model.put("parent", parent);

		Visit visit = new Visit();
		child.addVisit(visit);
		return visit;
	}

	// Spring MVC calls method loadChildWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/parents/{parentId}/children/{childId}/visits/new")
	@CedarAuthorization(action = "ViewClient", resourceType = "Parent", validate = true)
	@CedarAuthorization(action = "ViewClient", resourceType = "Child", validate = true)
	@CedarAuthorization(action = "EditClient", resourceType = "Child", validate = true)
	@CedarAuthorization(action = "AddClient", resourceType = "Child", validate = true)
	public String initNewVisitForm(Child child, HttpSession session) {
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
		EntityUID resource = EntityUID.parse("ChildClinic::Child::\"" + child.getName() + "\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));
		Map<String, Value> contextMap = new HashMap<>();
		CedarRequest cedarReq = new CedarRequest(principal, action, resource, contextMap, true);
		ResponseEntity<String> response = cedarService.checkAccess(cedarReq);
		if (!response.getBody().startsWith("Access Granted.")) {
			throw new SecurityException("Access Denied to modify Child.");
		}

		return "children/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadChildWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/parents/{parentId}/children/{childId}/visits/new")
	@CedarAuthorization(action = "EditClient", resourceType = "Child", validate = true)
	public String processNewVisitForm(@ModelAttribute Parent parent, @PathVariable("childId") int childId, @Valid Visit visit,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "children/createOrUpdateVisitForm";
		}

		parent.addVisit(childId, visit);
		this.parents.save(parent);
		redirectAttributes.addFlashAttribute("message", "Your visit has been booked.");
		return "redirect:/parents/{parentId}";
	}

}
