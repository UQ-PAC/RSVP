package uq.pac.rsvp.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Support class used to calculate line/column from an offset
 */
public class FileSource {
    // Keep track of line information for a file or a chunk of text broken down into lines
    // The [0] index is always 0
    // Each successive index represents a line number and mapped to the largest byte offset
    private final int [] data;
    private final String file;

    public FileSource(String file, List<Integer> lines) {
        this.file = file;
        this.data = new int[lines.size() + 1];
        for (int i = 1; i < data.length; i++) {
            this.data[i] = data[i-1] + lines.get(i - 1);
        }
    }

    // Find the least element >= offset
    // Binary search to find the least element less or equals to the offset
    private int find(int offset) {
        int min = 1; int max = data.length - 1;
        int result = -1;
        while (max >= min) {
            int pos = (min + max) / 2;
            if (offset <= data[pos]) {
                max = pos - 1;
                result = pos;
            } else {
                min = pos + 1;
            }
        }
        return result;
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
        if (line < 1 || line >= data.length) {
            throw new RuntimeException("Invalid line: " + line);
        }
        int offset = data[line - 1] + column;
        // The column exceeds line length
        if (offset > data[line] || !isValid(offset)) {
            throw new RuntimeException("Invalid column: " + column);
        }
        return offset;
    }

    // Split a string into a list of lines, accounts for leading
    // and trailing empty lines
    private static List<Integer> split(String contents) {
        List<Integer> lines = new ArrayList<>();
        int length = 0;
        for (char c : contents.toCharArray()) {
            length++;
            if (c == '\n') {
                lines.add(length);
                length = 0;
            }
        }
        if (length > 0) {
            lines.add(length);
        }
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
        int line = find(position);
        int column = position - data[line - 1];
        return new LineLoc(line, column);
    }

    public boolean isValid(int offset) {
        return offset > 0 && offset <= data[data.length - 1];
    }

    @Override
    public String toString() {
        return file + ":" +  data[data.length - 1] + " " + Arrays.toString(data);
    }
}
