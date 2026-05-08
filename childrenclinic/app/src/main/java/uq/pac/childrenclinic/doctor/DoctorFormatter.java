package uq.pac.childrenclinic.doctor;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class DoctorFormatter implements Formatter<Doctor> {

	private final DoctorRepository doctors;

	public DoctorFormatter(DoctorRepository doctors) {
		this.doctors = doctors;
	}

	@Override
	public String print(Doctor doctor, Locale locale) {
		if (doctor == null || doctor.getId() == null) {
			return "";
		}
		return doctor.getId().toString();
	}

	@Override
	public Doctor parse(String text, Locale locale) throws ParseException {
		if (text == null || text.trim().isEmpty()) {
			throw new ParseException("Doctor identifier cannot be empty or null.", 0);
		}

		try {
			Integer id = Integer.valueOf(text);
			return this.doctors.findById(id)
				.orElseThrow(() -> new ParseException(
						"Doctor entity not found in persistence context for identifier: " + text, 0));
		}
		catch (NumberFormatException exception) {
			throw new ParseException("Submitted Doctor identifier violates numerical formatting constraints: " + text,
					0);
		}
	}

}
