package uq.pac.childclinic.system;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class ClinicFormatter implements Formatter<Clinic> {
    private final ClinicRepository clinics;

	public ClinicFormatter(ClinicRepository clinics) {
		this.clinics = clinics;
	}

	@Override
	public String print(Clinic clinic, Locale locale) {
		String name = clinic.getClinicName();
		return (name != null) ? name : "<null>";
	}

	@Override
	public Clinic parse(String text, Locale locale) throws ParseException {
		Collection<Clinic> findClinics = this.clinics.findClinics();
		for (Clinic clinic : findClinics) {
			if (Objects.equals(clinic.getClinicName(), text)) {
				return clinic;
			}
		}
		throw new ParseException("Clinic not found: " + text, 0);
	}
}
