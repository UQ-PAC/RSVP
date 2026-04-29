package uq.pac.childrenclinic.secretary;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Secretaries {

	private List<Secretary> secretaries;

	@XmlElement
	public List<Secretary> getSecretariesList() {
		if (secretaries == null) {
			secretaries = new ArrayList<>();
		}
		return secretaries;
	}

}
