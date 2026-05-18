package uq.pac.childrenclinic.guardian;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class GuardianFormatter implements Formatter<Guardian> {

	private final GuardianRepository guardians;

	public GuardianFormatter(GuardianRepository guardians) {
		this.guardians = guardians;
	}

	@Override
	public String print(Guardian guardian, Locale locale) {
		if (guardian == null || guardian.getId() == null) {
			return "";
		}
		return guardian.getId().toString();
	}

	@Override
	public Guardian parse(String text, Locale locale) throws ParseException {
		if (text == null || text.trim().isEmpty()) {
			throw new ParseException("Guardian identifier cannot be empty or null.", 0);
		}

		try {
			Integer id = Integer.valueOf(text);
			return this.guardians.findById(id)
				.orElseThrow(() -> new ParseException(
						"Guardian entity not found in persistence context for identifier: " + text, 0));
		}
		catch (NumberFormatException exception) {
			throw new ParseException("Submitted Guardian identifier violates numerical formatting constraints: " + text,
					0);
		}
	}

}
