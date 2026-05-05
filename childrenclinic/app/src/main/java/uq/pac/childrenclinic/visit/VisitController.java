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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import uq.pac.childrenclinic.adult.Adult;
import uq.pac.childrenclinic.adult.AdultRepository;
import uq.pac.childrenclinic.cedar.CedarAuthorization;
import uq.pac.childrenclinic.cedar.CedarDeniedException;
import uq.pac.childrenclinic.cedar.CedarProgrammaticEvaluator;
import uq.pac.childrenclinic.doctor.Doctor;
import uq.pac.childrenclinic.doctor.DoctorRepository;
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

	private final DoctorRepository doctors;

	private final CedarProgrammaticEvaluator cedarEvaluator;

	public VisitController(PatientRepository patients, ConfidentialityRepository confidentialities,
			ClinicRepository clinics, AdultRepository adults, DoctorRepository doctors,
			CedarProgrammaticEvaluator cedarEvaluator) {
		this.patients = patients;
		this.confidentialities = confidentialities;
		this.clinics = clinics;
		this.adults = adults;
		this.doctors = doctors;
		this.cedarEvaluator = cedarEvaluator;
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
	public Collection<Clinic> populateClinics(HttpSession session) {
		Collection<Clinic> allClinics = this.clinics.findClinics();
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		return allClinics.stream().filter(clinic -> {
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var result = cedarEvaluator.evaluate(principal, "ViewClinic", "Clinic", cedarClinicId, "Item");
			return result.isGranted();
		}).collect(Collectors.toList());
	}

	@ModelAttribute("patient")
	public Patient findPatient(@PathVariable("patientId") int patientId) {
		return this.patients.findById(patientId).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
	}

	@ModelAttribute("adults")
	public Collection<Adult> populateAdults() {
		return this.adults.findAll();
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
	public String processNewVisitForm(@ModelAttribute Patient patient, @Valid Visit visit, BindingResult result,
			RedirectAttributes redirectAttributes, HttpSession session) {
		EntityUID principal = cedarEvaluator.resolvePrincipal(session);

		String resourceName = patient.getFirstName() + " " + patient.getLastName();
		var patientEval = cedarEvaluator.evaluate(principal, "EditPatient", "Patient", resourceName, "Page");
		if (!patientEval.isGranted())
			throw new CedarDeniedException("Access Denied.\n" + patientEval.responseBody());

		boolean isAuthorized = true;
		List<String> denialReasons = new ArrayList<>();

		Collection<Clinic> submittedClinics = visit.getClinics();
		if (submittedClinics == null || submittedClinics.isEmpty()) {
			throw new CedarDeniedException("You must assign the visit to at least one valid clinic.");
		}

		for (Clinic clinic : submittedClinics) {
			String cedarClinicId = clinic.getClinicName().replaceFirst("^Clinic\\s+", "");
			var clinicEval = cedarEvaluator.evaluate(principal, "EditPatient", "Clinic", cedarClinicId, "Page");

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

		if (result.hasErrors()) {
			return "patients/createOrUpdateVisitForm";
		}

		patient.addVisit(visit);
		this.patients.save(patient);
		redirectAttributes.addFlashAttribute("message", "Visit has been booked.");
		return "redirect:/patients/{patientId}";
	}

}
