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

package uq.pac.childrenclinic.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import uq.pac.childrenclinic.system.User;
import uq.pac.childrenclinic.system.UserRepository;

@ControllerAdvice
public class GlobalModelAttributes {

	private final UserRepository userRepository;

	private static final Logger logger = LoggerFactory.getLogger(GlobalModelAttributes.class);

	public GlobalModelAttributes(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@ModelAttribute
	public void populateLayoutData(HttpSession session, HttpServletRequest request, Model model) {
		String sessionUser = (String) session.getAttribute("currentUser");

		final String activeUsername = (sessionUser != null) ? sessionUser : "Guest";

		logger.info("activeUsername: {}", activeUsername);

		User currentUser = this.userRepository.findByUsername(activeUsername).orElseGet(() -> {
			User transientUser = new User();
			transientUser.setUsername(activeUsername);
			return transientUser;
		});

		Iterable<User> dynamicUsers = this.userRepository.findAll();

		model.addAttribute("currentUser", currentUser);
		model.addAttribute("dynamicUsers", dynamicUsers);

		request.setAttribute("currentUser", currentUser);
		request.setAttribute("dynamicUsers", dynamicUsers);
	}

}
