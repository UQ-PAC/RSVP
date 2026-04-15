package uq.pac.rsvp.support;

import java.lang.reflect.Type;
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

    public record LineLoc(int line, int column) {
        @Override
        public String toString() {
            return line + ":" + column;
        }
    }

    public final String file;
    public final int offset;
    public final int len;

    private final LineLoc start;
    private final LineLoc end;

    public SourceLoc(FileSource source, int offset, int len) {
        this.offset = offset;
        this.len = len;

        if (source != null) {
            this.file = source.getFile();
            this.start = source.getLineLoc(offset);
            this.end = source.getLineLoc(offset);
        } else {
            this.file = null;
            this.start = null;
            this.end = null;
        }
    }

    private SourceLoc() {
        this(null, -1, 0);
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
        return file + ":" + start + ":" + len;
    }

    public static class SourceLocDeserializer implements JsonDeserializer<SourceLoc> {
        private final FileSource fs;

        public SourceLocDeserializer(String filename, String contents) {
            this.fs = FileSource.get(filename, contents);
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
                FileSource fs = FileSource.get(Path.of(sourceFilename.getAsString()));
                return new SourceLoc(fs, offset, len);
            }
            return SourceLoc.MISSING;
        }
    }
}
