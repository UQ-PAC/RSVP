package uq.pac.childrenclinic.adult;

import org.springframework.core.style.ToStringCreator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import uq.pac.childrenclinic.model.Person;

@Entity
@Table(name = "adults")
@PrimaryKeyJoinColumn(name = "entity_id")
public class Adult extends Person {

	@Column(name = "telephone")
	@NotBlank
	@Pattern(regexp = "^\\+?[0-9\\-\\s]{10,20}$", message = "{telephone.invalid}")
	private String telephone;

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
