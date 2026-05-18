package uq.pac.rsvp.verification.impact;

import uq.pac.rsvp.support.SourceLoc;

import java.util.List;

public record RequestSummary(String summary, List<SourceLoc> locations) {
}
