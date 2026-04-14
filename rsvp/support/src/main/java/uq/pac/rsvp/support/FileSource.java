package uq.pac.rsvp.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Support class used to calculate line/column from an offset
 */
public class FileSource {
    private final int [] data;
    private final int max;
    private final String file;

    private FileSource(String file, List<String> lines) {
        this.file = file;
        this.data = new int[lines.size() + 1];
        for (int i = 1; i < data.length; i++) {
            this.data[i] = lines.get(i - 1).length() + 1;
        }
        this.max = Arrays.stream(data).sum();
    }

    public static FileSource get(String file, String contents) {
        if (contents == null) {
            return null;
        }
        String filename = file == null ? "unknown" : file;
        List<String> lines = Arrays.stream(contents.split("\\r?\\n")).toList();
        return new FileSource(filename, lines);
    }

    public static FileSource get(Path filename) {
        try {
            return filename != null && Files.isRegularFile(filename) && Files.isReadable(filename) ?
                    new FileSource(filename.toString(), Files.readAllLines(filename)) : null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }

    public String getFile() {
        return file;
    }

    public SourceLoc.LineLoc getLineLoc(int offset) {
        if (offset > max) {
            throw new RuntimeException("Offset %d out of bounds".formatted(offset));
        }

        int [] loc = new int []  { 1, 1 };
        int line = 1;

        while (offset > data[line]) {
            loc[0]++;
            offset -= data[line];
            line++;
        }

        loc[1] += offset - 1;
        return new SourceLoc.LineLoc(loc[0], loc[1]);
    }

    public boolean isWithinBounds(int offset) {
        return offset <= max;
    }
}
