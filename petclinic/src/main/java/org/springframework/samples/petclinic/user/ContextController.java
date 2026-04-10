package org.springframework.samples.petclinic.user;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContextController {

	@GetMapping("/switch-user")
	public String switchUser(@RequestParam("username") String username, HttpSession session) {
		session.setAttribute("currentUser", username);
		return "redirect:/";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}

}
