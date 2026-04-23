package uq.pac.rsvp.policy.ast.deserilisation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.SourceLoc;

public class SchemaJsonParser {

    private static Gson getSchemaGson(String filename, String content) {
        FileSource fs = content == null ? null : new FileSource(filename, content);
        return new GsonBuilder()
                .registerTypeAdapter(CommonTypeDefinition.class, new CommonTypeDefinitionDeserialiser())
                .registerTypeAdapter(Schema.class, new SchemaDeserialiser())
                .registerTypeAdapter(SourceLoc.class, new SourceLocDeserializer(fs))
                .disableJdkUnsafe()
                .create();
    }

    public static Schema parseSchema(String filename, String json) {
        return getSchemaGson(filename, json).fromJson(json, Schema.class);
    }

    public static Schema parseSchema(String json) {
        return parseSchema(null, json);
    }
}
