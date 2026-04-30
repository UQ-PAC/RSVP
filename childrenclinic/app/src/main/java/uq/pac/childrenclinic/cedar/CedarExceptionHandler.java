package uq.pac.childrenclinic.cedar;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@ControllerAdvice
public class CedarExceptionHandler {

	private final CedarLogContext cedarLogContext;

	public CedarExceptionHandler(CedarLogContext cedarLogContext) {
		this.cedarLogContext = cedarLogContext;
	}

	@ExceptionHandler(CedarDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ModelAndView handleAuthorizationDenial(CedarDeniedException exception) {
		ModelAndView modelAndView = new ModelAndView("error");
		modelAndView.addObject("message", exception.getMessage());
		modelAndView.addObject("status", 403);

		List<String> logs = this.cedarLogContext.getLogs();
		modelAndView.addObject("cedarLogs", logs);
		this.cedarLogContext.clearLogs();

		return modelAndView;
	}

}
