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

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

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
