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

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
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

import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarDeniedException;
import uq.pac.childrenclinic.cedar.CedarEntitiesInvalidationEvent;
import uq.pac.childrenclinic.cedar.CedarProgrammaticEvaluator;
import uq.pac.childrenclinic.doctor.Doctor;
import uq.pac.childrenclinic.doctor.DoctorRepository;
import uq.pac.childrenclinic.guardian.Guardian;
import uq.pac.childrenclinic.guardian.GuardianRepository;
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

	private final GuardianRepository guardians;

	private final DoctorRepository doctors;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	private final ApplicationEventPublisher eventPublisher;

	public VisitController(PatientRepository patients, ConfidentialityRepository confidentialities,
			ClinicRepository clinics, GuardianRepository guardians, DoctorRepository doctors,
			CedarProgrammaticEvaluator cedarEvaluator, ApplicationEventPublisher eventPublisher) {
		this.patients = patients;
		this.confidentialities = confidentialities;
		this.clinics = clinics;
		this.guardians = guardians;
		this.doctors = doctors;
		this.cedarEvaluator = cedarEvaluator;
		this.eventPublisher = eventPublisher;
	}

	@InitBinder("visit")
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "patient");
	}

	@ModelAttribute("confidentialities")
	public Collection<Confidentiality> populateConfidentialities() {
		return this.confidentialities.findConfidentialities();
	}

	@ModelAttribute("clinics")
	public Collection<Clinic> populateClinics(HttpSession session) {
		Collection<Clinic> allClinics = this.clinics.findClinics();
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		if (allClinics == null)
			return new ArrayList<>();

		return allClinics.stream().filter(clinic -> {
			if (clinic == null || clinic.getClinicName() == null)
				return false;
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var result = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Item");
			return result.isGranted();
		}).collect(Collectors.toList());
	}

	@ModelAttribute("patient")
	public Patient findPatient(@PathVariable("patientId") int patientId) {
		return this.patients.findById(patientId).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
	}

	@ModelAttribute("guardians")
	public Collection<Guardian> populateGuardians() {
		return this.guardians.findAll();
	}

	@ModelAttribute("doctors")
	public Collection<Doctor> populateDoctors() {
		return this.doctors.findAll();
	}

	@GetMapping("/patients/{patientId}/visits/new")
	@CedarAuthorization(action = "EditPatient", resourceType = "Patient", validate = true)
	public String initNewVisitForm(@PathVariable("patientId") int patientId, Model model) {
		Patient patient = this.patients.findById(patientId).orElseThrow();
		Visit visit = new Visit();
		patient.addVisit(visit);
		model.addAttribute("visit", visit);
		return "patients/createOrUpdateVisitForm";
	}

	@PostMapping("/patients/{patientId}/visits/new")
	public String processNewVisitForm(@PathVariable("patientId") int patientId, @Valid Visit visit,
			BindingResult result, RedirectAttributes redirectAttributes, HttpSession session) {

		Patient patient = this.patients.findById(patientId)
			.orElseThrow(() -> new IllegalArgumentException("Patient not found for identifier: " + patientId));
		visit.setPatient(patient);

		// Check binding/validation errors.
		if (result.hasErrors()) {
			return "patients/createOrUpdateVisitForm";
		}

		// Cedar authorization: can the principal edit this specific patient?
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		String resourceName = patient.getFirstName() + " " + patient.getLastName();
		var patientEval = cedarEvaluator.evaluate(principal, "EditPatient", "Patient", resourceName, "Page");
		if (!patientEval.isGranted())
			throw new CedarDeniedException("Access Denied.\n" + patientEval.responseBody());

		// Check for empty clinics.
		Collection<Clinic> submittedClinics = visit.getClinics();
		if (submittedClinics == null || submittedClinics.isEmpty()) {
			throw new CedarDeniedException("You must assign the visit to at least one valid clinic.");
		}

		if (visit.getGuardians() == null || visit.getGuardians().isEmpty()) {
			result.rejectValue("guardians", "NotEmpty", "At least one guardian must be assigned.");
		}

		if (visit.getDoctors() == null || visit.getDoctors().isEmpty()) {
			result.rejectValue("doctors", "NotEmpty", "At least one doctor must be assigned.");
		}
		if (result.hasErrors()) {
			return "patients/createOrUpdateVisitForm";
		}

		// Return the form if any validation errors accumulated so far.
		if (result.hasErrors()) {
			return "patients/createOrUpdateVisitForm";
		}

		// Cedar authorization on submitted clinics.
		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		for (Clinic clinic : submittedClinics) {
			if (clinic == null || clinic.getClinicName() == null)
				continue;
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			// Here we check for the "AddPatient" action, instead of "EditPatient",
			// since the former applies to the "Clinic" resource.
			var clinicEval = cedarEvaluator.evaluate(principal, "AddPatient", "Clinic", cedarClinicId, "Page");

			if (!clinicEval.isGranted()) {
				isAuthorized = false;
				if (clinicEval.responseBody() != null)
					denialReasons.add(clinicEval.responseBody());
			}
		}

		if (!isAuthorized) {
			String prefix = "Access Denied.\n";
			StringBuilder exceptionBody = new StringBuilder("Access Denied by the Cedar Policy Engine.\n\n");
			for (String reason : denialReasons)
				exceptionBody.append(reason.replaceAll("(?m)^" + prefix, "")).append("\n");
			throw new CedarDeniedException(exceptionBody.toString().trim());
		}

		// Final error check.
		if (result.hasErrors()) {
			return "patients/createOrUpdateVisitForm";
		}

		patient.addVisit(visit);
		this.patients.save(patient);
		eventPublisher.publishEvent(new CedarEntitiesInvalidationEvent(this));
		redirectAttributes.addFlashAttribute("message", "Visit has been booked.");
		return "redirect:/patients/{patientId}";
	}

}
