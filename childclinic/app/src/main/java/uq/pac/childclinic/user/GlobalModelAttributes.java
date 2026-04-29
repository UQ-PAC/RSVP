package uq.pac.childclinic.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import uq.pac.childclinic.system.User;
import uq.pac.childclinic.system.UserRepository;

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

		System.out.println(System.lineSeparator());
		System.out.println("activeUsername: " + activeUsername);
		System.out.println(System.lineSeparator());

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
