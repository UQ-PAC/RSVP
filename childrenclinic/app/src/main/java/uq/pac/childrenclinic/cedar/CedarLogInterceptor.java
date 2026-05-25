/*
 * Copyright 2026 Gabriel Henrique Lopes Gomes Alves Nunes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
