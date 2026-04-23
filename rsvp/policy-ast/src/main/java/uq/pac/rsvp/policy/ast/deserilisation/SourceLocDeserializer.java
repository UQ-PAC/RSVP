package uq.pac.rsvp.policy.ast.deserilisation;

import com.google.gson.*;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceLocDeserializer implements JsonDeserializer<SourceLoc> {
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
