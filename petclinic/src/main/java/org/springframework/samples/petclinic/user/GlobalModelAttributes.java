package org.springframework.samples.petclinic.user;

import jakarta.servlet.http.HttpSession;

import org.springframework.samples.petclinic.system.User;
import org.springframework.samples.petclinic.system.UserRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

	private final UserRepository userRepository;

	public GlobalModelAttributes(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@ModelAttribute("currentUser")
	public User populateUser(HttpSession session) {
		String sessionUser = (String) session.getAttribute("currentUser");

		final String activeUsername = (sessionUser != null) ? sessionUser : "Guest";

		System.out.println("activeUsername: " + activeUsername);

		return this.userRepository.findByUsername(activeUsername)
			.orElseGet(() -> {
				User transientUser = new User();
				transientUser.setUsername(activeUsername);
				return transientUser;
			});
	}

	@ModelAttribute("dynamicUsers")
	public Iterable<User> populateDynamicUsers() {
		return this.userRepository.findAll();
	}

}
