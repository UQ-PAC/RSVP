package uq.pac.childrenclinic.patient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.style.ToStringCreator;

import uq.pac.childrenclinic.doctor.Doctor;
import uq.pac.childrenclinic.model.Person;
import uq.pac.childrenclinic.visit.Visit;

@Entity
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "entity_id")
public class Patient extends Person {

	@Column(name = "address")
	@NotBlank
	private String address;

	@Column(name = "city")
	@NotBlank
	private String city;

	@OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<PatientAdult> responsibleAdults = new LinkedHashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "patient_doctors",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "doctor_id"))
    private Set<Doctor> doctors = new LinkedHashSet<>();

	@OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy("date ASC")
	private final Set<Visit> visits = new LinkedHashSet<>();

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Set<PatientAdult> getResponsibleAdults() {
		return responsibleAdults;
	}

	public void setResponsibleAdults(Set<PatientAdult> responsibleAdults) {
		this.responsibleAdults = responsibleAdults;
	}

	public Set<Doctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(Set<Doctor> doctors) {
        this.doctors = doctors;
    }

	public Collection<Visit> getVisits() {
		return this.visits;
	}

	public void addVisit(Visit visit) {
		getVisits().add(visit);
		visit.setPatient(this);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("new", this.isNew())
			.append("lastName", this.getLastName())
			.append("firstName", this.getFirstName())
			.append("birthDate", this.getBirthDate())
			.append("gender", this.getGender())
			.append("address", this.getAddress())
			.append("city", this.getCity())
			.append("visits", this.getVisits())
			.append("responsibleAdults", this.getResponsibleAdults())
			.toString();
	}

}
