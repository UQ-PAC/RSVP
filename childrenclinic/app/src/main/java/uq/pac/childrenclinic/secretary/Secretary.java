package uq.pac.childrenclinic.secretary;

import org.springframework.core.style.ToStringCreator;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import uq.pac.childrenclinic.model.Person;

@Entity
@Table(name = "secretaries")
@PrimaryKeyJoinColumn(name = "entity_id")
public class Secretary extends Person {

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("new", this.isNew())
			.append("lastName", this.getLastName())
			.append("firstName", this.getFirstName())
			.toString();
	}

}
