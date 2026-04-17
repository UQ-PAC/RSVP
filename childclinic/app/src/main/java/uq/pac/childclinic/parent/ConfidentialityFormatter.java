package uq.pac.childclinic.parent;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class ConfidentialityFormatter implements Formatter<Confidentiality> {
    private final ConfidentialityRepository confidentialities;

	public ConfidentialityFormatter(ConfidentialityRepository confidentialities) {
		this.confidentialities = confidentialities;
	}

	@Override
	public String print(Confidentiality confidentiality, Locale locale) {
		String name = confidentiality.getName();
		return (name != null) ? name : "<null>";
	}

	@Override
	public Confidentiality parse(String text, Locale locale) throws ParseException {
		Collection<Confidentiality> findConfidentialities = this.confidentialities.findConfidentialities();
		for (Confidentiality confidentiality : findConfidentialities) {
			if (Objects.equals(confidentiality.getName(), text)) {
				return confidentiality;
			}
		}
		throw new ParseException("Confidentiality not found: " + text, 0);
	}
}
