package uq.pac.childclinic.adult;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class AdultFormatter implements Formatter<Adult> {

	private final AdultRepository adults;

	public AdultFormatter(AdultRepository adults) {
		this.adults = adults;
	}

	@Override
	public String print(Adult adult, Locale locale) {
		if (adult == null || adult.getId() == null) {
			return "";
		}
		return adult.getId().toString();
	}

	@Override
	public Adult parse(String text, Locale locale) throws ParseException {
		if (text == null || text.trim().isEmpty()) {
			throw new ParseException("Adult identifier cannot be empty or null.", 0);
		}

		try {
			Integer id = Integer.valueOf(text);
			return this.adults.findById(id)
				.orElseThrow(() -> new ParseException(
						"Adult entity not found in persistence context for identifier: " + text, 0));
		}
		catch (NumberFormatException exception) {
			throw new ParseException("Submitted Adult identifier violates numerical formatting constraints: " + text,
					0);
		}
	}

}
