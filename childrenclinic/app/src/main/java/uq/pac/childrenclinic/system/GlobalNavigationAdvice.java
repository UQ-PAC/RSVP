package uq.pac.childrenclinic.system;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import uq.pac.childrenclinic.cedar.CedarRequest;
import uq.pac.childrenclinic.cedar.CedarService;

@ControllerAdvice(basePackages = "uq.pac.childrenclinic")
public class GlobalNavigationAdvice {

	private final CedarService cedarService;

	public GlobalNavigationAdvice(CedarService cedarService) {
		this.cedarService = cedarService;
	}

	@ModelAttribute
	public void populateNavigationPermissions(Model model, HttpSession session) {
		EntityUID principal = resolvePrincipal(session);

		boolean canListPatients = isAuthorized(principal, "ListPatients");
		boolean canListAdults = isAuthorized(principal, "ListAdults");
		boolean canListEmployees = isAuthorized(principal, "ListEmployees");

		model.addAttribute("navCanListPatients", canListPatients);
		model.addAttribute("navCanListAdults", canListAdults);
		model.addAttribute("navCanListEmployees", canListEmployees);
	}

	private EntityUID resolvePrincipal(HttpSession session) {
		String principalId = (String) session.getAttribute("currentUser");
		principalId = principalId == null ? "Guest" : principalId;

		if (principalId.equals("Guest")) {
			return EntityUID.parse("ChildrenClinic::Guest::\"Unknown\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
		else {
			return EntityUID.parse("ChildrenClinic::Employee::\"" + principalId + "\"")
				.orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
		}
	}

	private boolean isAuthorized(EntityUID principal, String actionStr) {
		EntityUID action = EntityUID.parse("ChildrenClinic::Action::\"" + actionStr + "\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
		EntityUID resource = EntityUID.parse("ChildrenClinic::Clinic::\"Any\"")
			.orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

		String access = cedarService.checkAccess(new CedarRequest(principal, action, resource, new HashMap<>(), true))
			.getBody();
		return access != null && access.startsWith("Access Granted.");
	}

}