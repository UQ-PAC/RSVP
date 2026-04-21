package uq.pac.rsvp.support;

import java.lang.reflect.Type;

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

    public final int line;
    public final int col;

    public SourceLoc(String file, int offset, int len, int line, int col) {
        this.file = file;
        this.offset = offset;
        this.len = len;
        this.line = line;
        this.col = col;
    }

    public SourceLoc() {
        file = "unknown";
        offset = -1;
        len = 0;
        line = -1;
        col = -1;
    }

    @Override
    public String toString() {
        return file + ":" + line + ":" + col + ":" + len;
    }

    public static class SourceLocDeserializer implements JsonDeserializer<SourceLoc> {

        private final String filename;
        private final String content;

        public SourceLocDeserializer(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }

        @Override
        public SourceLoc deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject sourceObject = json.getAsJsonObject();
            JsonElement configuiredFilename = sourceObject.get("file");

            int offset = sourceObject.get("offset").getAsInt();
            int len = sourceObject.get("len").getAsInt();

            int line = 1;
            int col = 1;

            boolean found = false;

            for (int i = 0; i < content.length(); i++) {
                if (i == offset) {
                    found = true;
                    break;
                } else if (content.charAt(i) == '\n') {
                    line++;
                    col = 1;
                } else {
                    col++;
                }
            }

            if (!found) {
                line = -1;
                col = -1;
            }

            if (configuiredFilename != null) {
                return new SourceLoc(configuiredFilename.getAsString(), offset, len, line, col);
            } else if (filename != null) {
                return new SourceLoc(filename, offset, len, line, col);
            } else {
                return new SourceLoc("unknown", offset, len, line, col);
            }
        }
    }
}
