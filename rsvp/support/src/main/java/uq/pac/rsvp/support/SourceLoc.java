/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.support;

import java.util.Objects;

/**
 * Describes a location (range) within a textual source, given by offset
 * (0-based) and length,
 * as well as line and column (1-based).
 */
public class SourceLoc {

    public static final SourceLoc MISSING = new SourceLoc(null, -1, 0, null, null);

    public final String file;
    public final int offset;
    public final int len;
    private final LineLoc start;
    private final LineLoc end;

    SourceLoc(String file, int offset, int len, LineLoc start, LineLoc end) {
        this.file = file;
        this.offset = offset;
        this.len = len;
        this.start = start;
        this.end = end;
    }

    public String getFile() {
        return file;
    }

    public LineLoc getStartLoc() {
        return start;
    }

    public LineLoc getEndLoc() {
        return end;
    }

    public String toString(boolean includeFile) {
        if (this.isEmpty()) {
            return "<unknown>";
        }
        String loc = "%d:%d".formatted(offset, len);
        if (start != null && end != null) {
            loc += " [%s-%s]".formatted(start.toString(), end.toString());
        }
        return includeFile ? file + ":" + loc : loc;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public boolean isEmpty() {
        return this.equals(MISSING);
    }

    public static SourceLoc empty() {
        return new SourceLoc(null, -1, 0, null, null);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other instanceof SourceLoc loc) {
            return Objects.equals(this.file, loc.file) &&
                    loc.offset == this.offset &&
                    loc.len == this.len &&
                    Objects.equals(loc.getStartLoc(), start) &&
                    Objects.equals(loc.getEndLoc(), end);
        }
        return false;
    }
}
