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
