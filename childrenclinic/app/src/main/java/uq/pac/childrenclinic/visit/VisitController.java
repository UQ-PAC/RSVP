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
package uq.pac.childrenclinic.visit;

import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import uq.pac.childrenclinic.adult.Adult;
import uq.pac.childrenclinic.adult.AdultRepository;
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarService;
import uq.pac.childrenclinic.patient.Patient;
import uq.pac.childrenclinic.patient.PatientRepository;
import uq.pac.childrenclinic.system.Clinic;
import uq.pac.childrenclinic.system.ClinicRepository;

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

	private final PatientRepository patients;

	private final ConfidentialityRepository confidentialities;

	private final ClinicRepository clinics;

	private final AdultRepository adults;

	private final CedarService cedarService;

	public VisitController(PatientRepository patients, ConfidentialityRepository confidentialities,
			ClinicRepository clinics, AdultRepository adults, CedarService cedarService) {
		this.patients = patients;
		this.confidentialities = confidentialities;
		this.clinics = clinics;
		this.adults = adults;
		this.cedarService = cedarService;
	}

	@InitBinder("visit")
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

	@ModelAttribute("patient")
	public Patient findPatient(@PathVariable("patientId") int patientId) {
		return this.patients.findById(patientId).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
	}

	@ModelAttribute("adults")
	public Collection<Adult> populateAdults() {
		return this.adults.findAll();
	}

	@GetMapping("/patients/{patientId}/visits/new")
	@CedarAuthorization(action = "EditPatient", resourceType = "Patient", validate = true)
	// public String initNewVisitForm(@PathVariable("patientId") int patientId,
	// Map<String, Object> model) {
	public String initNewVisitForm(@PathVariable("patientId") int patientId, Model model) {
		Patient patient = this.patients.findById(patientId).orElseThrow();
		Visit visit = new Visit();
		patient.addVisit(visit);
		model.addAttribute("visit", visit);
		return "patients/createOrUpdateVisitForm";
	}

	@PostMapping("/patients/{patientId}/visits/new")
	@CedarAuthorization(action = "EditPatient", resourceType = "Patient", validate = true)
	public String processNewVisitForm(@ModelAttribute Patient patient, @Valid Visit visit, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors())
			return "patients/createOrUpdateVisitForm";
		patient.addVisit(visit);
		this.patients.save(patient);
		redirectAttributes.addFlashAttribute("message", "Your visit has been booked.");
		return "redirect:/patients/{patientId}";
	}

	// /**
	// * Called before each and every @RequestMapping annotated method. 2 goals: - Make
	// sure
	// * we always have fresh data - Since we do not use the session scope, make sure that
	// * Child object always has an id (Even though id is not part of the form fields)
	// * @param childId
	// * @return Child
	// */
	// @ModelAttribute("visit")
	// public Visit loadChildWithVisit(@PathVariable("parentId") int parentId,
	// @PathVariable("childId") int childId,
	// Map<String, Object> model) {
	// Optional<Parent> optionalParent = parents.findById(parentId);
	// Parent parent = optionalParent.orElseThrow(() -> new IllegalArgumentException(
	// "Parent not found with id: " + parentId + ". Please ensure the ID is correct."));

	// Child child = parent.getChild(childId);
	// if (child == null) {
	// throw new IllegalArgumentException(
	// "Child with id " + childId + " not found for parent with id " + parentId + ".");
	// }
	// model.addAttribute("child", child);
	// model.addAttribute("parent", parent);

	// Visit visit = new Visit();
	// child.addVisit(visit);
	// return visit;
	// }

	// // Spring MVC calls method loadChildWithVisit(...) before initNewVisitForm is
	// // called
	// @GetMapping("/parents/{parentId}/children/{childId}/visits/new")
	// @CedarAuthorization(action = "ViewPatient", resourceType = "Parent", validate =
	// true)
	// @CedarAuthorization(action = "ViewPatient", resourceType = "Child", validate =
	// true)
	// @CedarAuthorization(action = "EditPatient", resourceType = "Child", validate =
	// true)
	// @CedarAuthorization(action = "AddPatient", resourceType = "Child", validate = true)
	// public String initNewVisitForm(Child child, HttpSession session) {
	// String principalId = (String) session.getAttribute("currentUser");
	// if (session.getAttribute("currentUser") == null) {
	// principalId = "Guest";
	// }

	// System.out.println("Cookie principalId: " + principalId);

	// EntityUID principal;
	// if (principalId.equals("Guest")) {
	// principal = EntityUID.parse("ChildrenClinic::Guest::\"Unknown\"")
	// .orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
	// }
	// else {
	// principal = EntityUID.parse("ChildrenClinic::Employee::\"" + principalId + "\"")
	// .orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
	// }

	// EntityUID action = EntityUID.parse("ChildrenClinic::Action::\"" + "EditPatient" +
	// "\"")
	// .orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
	// EntityUID resource = EntityUID.parse("ChildrenClinic::Child::\"" + child.getName()
	// + "\"")
	// .orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));
	// Map<String, Value> contextMap = new HashMap<>();
	// CedarRequest cedarReq = new CedarRequest(principal, action, resource, contextMap,
	// true);
	// ResponseEntity<String> response = cedarService.checkAccess(cedarReq);
	// if (!response.getBody().startsWith("Access Granted.")) {
	// throw new SecurityException("Access Denied to modify Child.");
	// }

	// return "children/createOrUpdateVisitForm";
	// }

	// // Spring MVC calls method loadChildWithVisit(...) before processNewVisitForm is
	// // called
	// @PostMapping("/parents/{parentId}/children/{childId}/visits/new")
	// @CedarAuthorization(action = "EditPatient", resourceType = "Child", validate =
	// true)
	// public String processNewVisitForm(@ModelAttribute Parent parent,
	// @PathVariable("childId") int childId, @Valid Visit visit,
	// BindingResult result, RedirectAttributes redirectAttributes) {
	// if (result.hasErrors()) {
	// return "children/createOrUpdateVisitForm";
	// }

	// parent.addVisit(childId, visit);
	// this.parents.save(parent);
	// redirectAttributes.addFlashAttribute("message", "Your visit has been booked.");
	// return "redirect:/parents/{parentId}";
	// }

}
