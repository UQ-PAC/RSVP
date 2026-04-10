package uq.pac.childclinic.cedar;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CedarDeniedException extends RuntimeException {

	public CedarDeniedException(String message) {
		super(message);
	}

}
