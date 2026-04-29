package uq.pac.childclinic.patient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;

import uq.pac.childclinic.adult.Adult;
import uq.pac.childclinic.model.Gender;
import uq.pac.childclinic.model.Person;
import uq.pac.childclinic.visit.Visit;

@Entity
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "entity_id")
public class Patient extends Person {

	@Column(name = "birth_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull
	private LocalDate birthDate;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "gender_id")
	@NotNull
	private Gender gender;

	@Column(name = "address")
	@NotBlank
	private String address;

	@Column(name = "city")
	@NotBlank
	private String city;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "patient_adults", joinColumns = @JoinColumn(name = "patient_id"),
			inverseJoinColumns = @JoinColumn(name = "adult_id"))
	private Set<Adult> responsibleAdults = new LinkedHashSet<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "patient_id")
	@OrderBy("date ASC")
	private final Set<Visit> visits = new LinkedHashSet<>();

	public LocalDate getBirthDate() {
		return this.birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public Gender getGender() {
		return this.gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

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

	public Set<Adult> getResponsibleAdults() {
		return responsibleAdults;
	}

	public void setResponsibleAdults(Set<Adult> responsibleAdults) {
		this.responsibleAdults = responsibleAdults;
	}

	public Collection<Visit> getVisits() {
		return this.visits;
	}

	public void addVisit(Visit visit) {
		getVisits().add(visit);
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
