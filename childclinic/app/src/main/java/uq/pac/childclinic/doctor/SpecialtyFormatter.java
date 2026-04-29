package uq.pac.childclinic.doctor;

import java.text.ParseException;
import java.util.Locale;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyFormatter implements Formatter<Specialty> {

	private final SpecialtyRepository specialties;

	public SpecialtyFormatter(SpecialtyRepository specialties) {
		this.specialties = specialties;
	}

	@Override
	public String print(Specialty specialty, Locale locale) {
		return specialty.getName() != null ? specialty.getName() : "";
	}

	@Override
	public Specialty parse(String text, Locale locale) throws ParseException {
		return this.specialties.findAll()
			.stream()
			.filter(s -> s.getName().equals(text))
			.findFirst()
			.orElseThrow(() -> new ParseException("Specialty not found: " + text, 0));
	}

}
