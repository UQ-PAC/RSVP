package uq.pac.rsvp.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Support class used to calculate line/column from an offset
 */
public class FileSource {
    // Keep track of line information for a file or a chunk of text broken down into lines.
    // The [0] index is always 0. Each successive index represents a line number and mapped to the
    // largest byte offset. The final index marks the end position of the last line (the last line
    // may be empty, i.e. data[data.length-1] == data[data.length-2] is possible).
    private final int [] data;
    private final String file;

    public FileSource(String file, List<Integer> lines) {
        this.file = file;
        this.data = new int[lines.size() + 1];
        for (int i = 1; i < data.length; i++) {
            this.data[i] = data[i-1] + lines.get(i - 1);
        }
    }

    /**
     * Find the greatest element with value less or equal to the specified offset
     * via a binary search. (Ignores the last element, which marks the end of the final line).
     */
    private int find(int offset) {
        int min = 0; int max = data.length - 2;
        while (max > min) {
            int pos = min + (max - min) / 2 + 1;
            // Now: min < pos <= max
            if (offset < data[pos]) {
                max = pos - 1;
            } else {
                min = pos;
            }
        }

        return min;
    }

    /**
     * Translate line/column combination into the one-dimensional position,
     * e.g., consider file
     * <pre>
     *     abc\n
     *     def
     * </pre>
     * Then position for 1:1 ("a") is 1, 1:3 (c) is 3 and 2:1 (d) is 5
     */
    public int getPosition(int line, int column) {
        int position = getSourcePosition(line, column);
        if (position == -1) {
            throw new RuntimeException("Invalid location: %d:%d".formatted(line, column));
        }
        return position;
    }

    private int getSourcePosition(int line, int column) {
        if (line < 1 || line >= data.length || column < 1) {
            return -1;
        }

        int position = data[line - 1] + column;

        if (line == data.length - 1 || (line == data.length - 2 && data[line] == data[line + 1])) {
            // position is allowed to extend one-past-the-end so that end-of-file is valid
            if (position > (data[line] + 1)) {
                return -1;
            }
            return position; // (end-of-file)
        }

        if (position > data[line] || !isValid(position)) {
            // The column exceeds line length
            return -1;
        }

        return position;
    }

    /**
     * Split a string into a list of line lengths, accounting for leading and trailing empty
     * lines. If the string ends with a newline, the returned array will indicate an empty final
     * line (i.e. a trailing line with length 0).
     */
    private static List<Integer> split(String contents) {
        List<Integer> lines = new ArrayList<>();
        lines.add(0);
        int length = 0;
        for (char c : contents.toCharArray()) {
            length++;
            if (c == '\n') {
                lines.set(lines.size() - 1, length);
                lines.add(0);
                length = 0;
            }
        }
        lines.set(lines.size() - 1, length);
        return lines;
    }

    public FileSource(String file, String contents) {
        // Need to double-check this, but omitting '\r' from Windows-style line endings
        // should push carriage return to the string and be incorporated in the string length
        this(file, split(contents));
    }

    public FileSource(Path filename) throws IOException {
        this(filename.toString(), Files.readString(filename));
    }

    public String getFile() {
        return file;
    }

    public LineLoc getLineLoc(int position) {
        if (!isValid(position)) {
            throw new RuntimeException("Invalid position: %d".formatted(position));
        }
        int line = find(position - 1) + 1;
        int column = position - data[line - 1];
        return new LineLoc(line, column);
    }

    public boolean isValid(int position) {
        return position > 0 && position <= (data[data.length - 1] + 1);
    }

    @Override
    public String toString() {
        return file + ":" +  data[data.length - 1] + " " + Arrays.toString(data);
    }

    /**
     * Construct a source location describing a source code interval. The interval is constructed
     * from a zero-based offset (position before the first character in the interval)
     * and the length of the interval. Consider the following file
     * <pre>
     *     1 abc
     *     2 def
     * </pre>
     * String {@code "def"} starts at location "2:1" and ends at "2:3". The {@code offset} is then
     * 4 (considering the first line ends with a '\n') and the {@code length} is 3.
     *
     * @param offset zero-based char offset pointing to the first character of the range
     * @param len the length of the fragment
     * @throws RuntimeException if the offset or the length are out of bounds WRT the provided index
     */
    public SourceLoc getSourceLoc(int offset, int len) {
        // If line locations are computed, then offset and length
        // must be valid WRT file source. The offset itself is checked
        // by the file source, but we need to make sure length is valid
        if (len < 1 || !isValid(offset + 1) || !isValid(offset + len)) {
            throw new RuntimeException("Invalid source location: %s:%d:%d".formatted(getFile(), offset, len));
        }
        return new SourceLoc(file, offset, len,
                getLineLoc(offset + 1), getLineLoc(offset + len));
    }

    public SourceLoc getSourceLoc(LineLoc start, LineLoc end) {
        int offset = getPosition(start.line(), start.column()) - 1;
        int length = getPosition(end.line(), end.column()) - offset;
        return new SourceLoc(file, offset, length, start, end);
    }

    /**
     * Generate range starting at the location {@code start} and ending at {@code end}
     */
    public SourceLoc getSourceLoc(SourceLoc start, SourceLoc end) {
        // Ensure that the start location precedes the end one
        if (start.offset > end.offset || (start.offset == end.offset && start.len > end.len)) {
            throw new RuntimeException("End location %s precedes start location %s".formatted(end.toString(), start.toString()));
        }
        int offset = start.offset;
        int length = end.offset + end.len - offset;
        return getSourceLoc(offset, length);
    }

    /**
     * Check whether the source location is valid (assuming it has been created by the same FileSource).
     * The source location is valid if the file name matches the filename of this file source,
     * the offset/column combination describes a valid range and line/column pairs match those
     * computed from offset and length
     */
    public boolean isValid(SourceLoc loc) {
        if (!Objects.equals(this.file, loc.getFile()) ||
                loc.getEndLoc() == null || loc.getStartLoc() == null) {
            return false;
        }

        if (!isValid(loc.offset + 1) || !isValid(loc.offset + loc.len)) {
            return false;
        }

        return getLineLoc(loc.offset + 1).equals(loc.getStartLoc()) &&
                getLineLoc(loc.offset + loc.len).equals(loc.getEndLoc());
    }
}
