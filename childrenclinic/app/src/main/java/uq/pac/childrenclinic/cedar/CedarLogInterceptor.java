package uq.pac.childrenclinic.cedar;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CedarLogInterceptor implements HandlerInterceptor {

	private final CedarLogContext cedarLogContext;

	public CedarLogInterceptor(CedarLogContext cedarLogContext) {
		this.cedarLogContext = cedarLogContext;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (modelAndView != null && !isRedirectView(modelAndView)) {
			List<String> logs = this.cedarLogContext.getLogs();
			modelAndView.addObject("cedarLogs", logs);
			this.cedarLogContext.clearLogs();
		}
	}

	private boolean isRedirectView(ModelAndView modelAndView) {
		String viewName = modelAndView.getViewName();
		return viewName != null && viewName.startsWith("redirect:");
	}

}
