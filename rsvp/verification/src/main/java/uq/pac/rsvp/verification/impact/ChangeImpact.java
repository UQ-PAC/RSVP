package uq.pac.rsvp.verification.impact;

import java.util.List;

public record ChangeImpact(List<RequestSummary> permitted, List<RequestSummary> forbidden) {
}
