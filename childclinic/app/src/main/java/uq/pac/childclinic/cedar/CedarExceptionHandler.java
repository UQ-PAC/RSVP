package uq.pac.childclinic.cedar;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class CedarExceptionHandler {

	@ExceptionHandler(CedarDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ModelAndView handleAuthorizationDenial(CedarDeniedException exception) {
		ModelAndView modelAndView = new ModelAndView("error");
		modelAndView.addObject("message", exception.getMessage());
		modelAndView.addObject("status", 403);
		return modelAndView;
	}

}
