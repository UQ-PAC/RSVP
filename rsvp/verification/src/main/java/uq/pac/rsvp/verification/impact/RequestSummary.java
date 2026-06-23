/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.verification.impact;

import uq.pac.rsvp.support.SourceLoc;

import java.util.List;

public record RequestSummary(String summary, List<SourceLoc> locations) {
}
