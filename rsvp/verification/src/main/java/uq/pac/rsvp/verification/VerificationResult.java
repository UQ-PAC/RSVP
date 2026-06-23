/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.verification;

import uq.pac.rsvp.support.reporting.Report;

import java.util.Set;

/**
 * The results of analysis, including reports and change impact (if performed).
 */
public record VerificationResult(Set<Report> reports, VerificationCache cache) {
}
