package uq.pac.childrenclinic.adult;

import java.time.LocalDate;

import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import uq.pac.childrenclinic.model.Gender;
import uq.pac.childrenclinic.model.Person;

@Entity
@Table(name = "adults")
@PrimaryKeyJoinColumn(name = "entity_id")
public class Adult extends Person {

	@Column(name = "birth_date")
	@NotNull
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "gender_id")
	@NotNull
	private Gender gender;

	@Column(name = "telephone")
	@NotBlank
	@Pattern(regexp = "^\\+?[0-9\\-\\s]{10,20}$", message = "{telephone.invalid}")
	private String telephone;

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

	public String getTelephone() {
		return this.telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("new", this.isNew())
			.append("lastName", this.getLastName())
			.append("firstName", this.getFirstName())
			.append("telephone", this.getTelephone())
			.append("birthDate", this.getBirthDate())
			.append("gender", this.getGender())
			.toString();
	}

}
