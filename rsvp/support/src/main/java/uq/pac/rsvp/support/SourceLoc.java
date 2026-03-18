package uq.pac.rsvp.support;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class SourceLoc {

    public static final SourceLoc MISSING = new SourceLoc();

    public final String file;
    public final int offset;
    public final int len;

    public SourceLoc(String file, int offset, int len) {
        this.file = file;
        this.offset = offset;
        this.len = len;
    }

    public SourceLoc() {
        this.file = "unknown";
        this.offset = -1;
        this.len = 0;
    }

    @Override
    public String toString() {
        // FIXME: figure out line and char???
        return file + ":" + offset + ":" + len;
    }

    public static class SourceLocDeserializer implements JsonDeserializer<SourceLoc> {

        private final String filename;

        public SourceLocDeserializer(String filename) {
            this.filename = filename;
        }

        @Override
        public SourceLoc deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject sourceObject = json.getAsJsonObject();

            JsonElement configuiredFilename = sourceObject.get("file");

            int offset = sourceObject.get("offset").getAsInt();
            int len = sourceObject.get("len").getAsInt();

            if (configuiredFilename != null) {
                return new SourceLoc(configuiredFilename.getAsString(), offset, len);
            } else if (filename != null) {
                return new SourceLoc(filename, offset, len);
            } else {
                return new SourceLoc("unknown", offset, len);
            }
        }
    }
}
