package org.springframework.samples.petclinic.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.samples.petclinic.system.User;
import org.springframework.samples.petclinic.system.UserRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

	private final UserRepository userRepository;

	public GlobalModelAttributes(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@ModelAttribute
    public void populateLayoutData(HttpSession session, HttpServletRequest request, Model model) {
        String sessionUser = (String) session.getAttribute("currentUser");

        final String activeUsername = (sessionUser != null) ? sessionUser : "Guest";

		System.out.println("activeUsername: " + activeUsername);

        User currentUser = this.userRepository.findByUsername(activeUsername)
            .orElseGet(() -> {
                User transientUser = new User();
                transientUser.setUsername(activeUsername); 
                return transientUser;
            });

        Iterable<User> dynamicUsers = this.userRepository.findAll();

        // 1. Populate the Model for standard view rendering
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("dynamicUsers", dynamicUsers);

        // 2. Populate the Request Attributes to persist through exceptions and /error forwards
        request.setAttribute("currentUser", currentUser);
        request.setAttribute("dynamicUsers", dynamicUsers);
    }

}
