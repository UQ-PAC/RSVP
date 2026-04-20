package uq.pac.rsvp.support;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Describes a location (range) within a textual source, given by offset (0-based) and length,
 * as well as line and column (1-based).
 */
public class SourceLoc {

    public static final SourceLoc MISSING = new SourceLoc();

    public final String file;
    public final int offset;
    public final int len;

    private final LineLoc start;
    private final LineLoc end;

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
     * @param source file index used to compute line/column information from offsets.
     *               If the source argument is null, the location only stores offset and length,
     *               otherwise it tries to compute line/column interval
     * @param offset zero-based char offset pointing to the first character of the range
     * @param len the length of the fragment
     * @throws RuntimeException if the offset or the length are out of bounds WRT the provided index
     */
    public SourceLoc(FileSource source, int offset, int len) {
        this.offset = offset;
        this.len = len;

        if (source != null) {
            this.file = source.getFile();

            // If line locations are computed, then offset and length
            // must be valid WRT file source. The offset itself is checked
            // by the file source, but we need to make sure length is valid
            if (len < 1 || !source.isValid(offset + 1) || !source.isValid(offset + len)) {
                throw new RuntimeException("Invalid source location: %s:%d:%d".formatted(source.getFile(), offset, len));
            }

            this.start = source.getLineLoc(offset + 1);
            this.end = source.getLineLoc(offset + len);
        } else {
            this.file = null;
            this.start = null;
            this.end = null;
        }
    }

    /**
     * Private constructor generating invalid locations
     */
    private SourceLoc() {
        this(null, -1, 0);
    }

    /**
     * Specify all parameters of the locations manually
     */
    public SourceLoc(String file, int offset, int len, LineLoc start, LineLoc end) {
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

    @Override
    public String toString() {
        String loc = "%s:%d:%d".formatted(file, offset, len);
        if (start != null && end != null) {
            loc += " [%s-%s]".formatted(start.toString(), end.toString());
        }
        return loc;
    }

    public static class SourceLocDeserializer implements JsonDeserializer<SourceLoc> {
        private final FileSource fs;

        public SourceLocDeserializer(FileSource fs) {
            this.fs = fs;
        }

        @Override
        public SourceLoc deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject sourceObject = json.getAsJsonObject();
            JsonElement sourceFilename = sourceObject.get("file");

            int offset = sourceObject.get("offset").getAsInt();
            int len = sourceObject.get("len").getAsInt();

            if (fs != null) {
                return new SourceLoc(fs, offset, len);
            } else if (sourceFilename != null) {
                // The file has not been provided to the deserialiser, but it has been found
                // in the JSON specification. If so, try to read that file to extract its contents.
                // The file may not necessarily exist
                Path fn = Path.of(sourceFilename.getAsString());
                if (Files.exists(fn) && Files.isReadable(fn) && Files.isRegularFile(fn)) {
                    try {
                        FileSource fs = new FileSource(Path.of(sourceFilename.getAsString()));
                        return new SourceLoc(fs, offset, len);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return SourceLoc.MISSING;
        }
    }
}
