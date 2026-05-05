package uq.pac.childrenclinic.cedar;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.cedarpolicy.value.EntityUID;

import jakarta.servlet.http.HttpSession;

@Component
public class CedarProgrammaticEvaluator {

    private final CedarService cedarService;
    private final CedarLogContext cedarLogContext;

    public CedarProgrammaticEvaluator(CedarService cedarService, CedarLogContext cedarLogContext) {
        this.cedarService = cedarService;
        this.cedarLogContext = cedarLogContext;
    }

    /**
     * Standardizes how the Principal is resolved from the HTTP Session.
     */
    public EntityUID resolvePrincipal(HttpSession session) {
        String principalId = (String) session.getAttribute("currentUser");
        principalId = principalId == null ? "Guest" : principalId;

        if ("Guest".equals(principalId)) {
            return EntityUID.parse("ChildrenClinic::Guest::\"Unknown\"")
                .orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
        } else {
            return EntityUID.parse("ChildrenClinic::Employee::\"" + principalId + "\"")
                .orElseThrow(() -> new IllegalArgumentException("Invalid Principal UID format."));
        }
    }

    /**
     * Executes the Cedar evaluation and automatically pushes to the Log Context.
     */
    public EvaluationResult evaluate(EntityUID principal, String actionStr, String resourceType, String resourceId, String logPrefix) {
        EntityUID action = EntityUID.parse("ChildrenClinic::Action::\"" + actionStr + "\"")
                .orElseThrow(() -> new IllegalArgumentException("Invalid Action UID format."));
        
        EntityUID resource = EntityUID.parse("ChildrenClinic::" + resourceType + "::\"" + resourceId + "\"")
                .orElseThrow(() -> new IllegalArgumentException("Invalid Resource UID format."));

        CedarRequest request = new CedarRequest(principal, action, resource, new HashMap<>(), true);
        ResponseEntity<String> response = cedarService.checkAccess(request);
        String accessBody = response.getBody();

        String logEntry = logPrefix + " Request: Principal=" + principal + ", Action=" + action + ", Resource=" + resource
                + " | Response: " + accessBody;
        this.cedarLogContext.addLog(logEntry);

        boolean isGranted = response.getStatusCode().is2xxSuccessful() && accessBody != null && accessBody.startsWith("Access Granted.");
        
        return new EvaluationResult(isGranted, accessBody);
    }

    // A simple record to return both the boolean status and the textual reason.
    public record EvaluationResult(boolean isGranted, String responseBody) {}
}
