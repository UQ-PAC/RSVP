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
	public String populateUser(HttpSession session) {
		String currentUser = (String) session.getAttribute("currentUser");
		return (currentUser != null) ? currentUser : "Guest";
	}

	@ModelAttribute("dynamicUsers")
	public Iterable<User> populateDynamicUsers() {
		return this.userRepository.findAll();
	}

}
