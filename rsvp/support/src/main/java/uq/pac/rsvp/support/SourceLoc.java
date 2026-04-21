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

    public static final SourceLoc MISSING =
            new SourceLoc(null, -1, 0, null, null);

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
                return fs.getSourceLoc(offset, len);
            } else if (sourceFilename != null) {
                // The file has not been provided to the deserialiser, but it has been found
                // in the JSON specification. If so, try to read that file to extract its contents.
                // The file may not necessarily exist
                Path fn = Path.of(sourceFilename.getAsString());
                if (Files.exists(fn) && Files.isReadable(fn) && Files.isRegularFile(fn)) {
                    try {
                        FileSource fs = new FileSource(Path.of(sourceFilename.getAsString()));
                        return fs.getSourceLoc(offset, len);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return SourceLoc.MISSING;
        }
    }
}
