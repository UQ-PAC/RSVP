package uq.pac.rsvp.policy.ast.entity;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import uq.pac.rsvp.support.FileSource;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Parse a JSON specification describing a set of Cedar entities and output then as an {@link EntitySet}
 */
class JacksonEntityReader {

    private int position() {
        return 0;
    }

    private final FileSource source;
    private final JsonParser reader;

    public JacksonEntityReader(Path file) throws IOException {
        this.source = new FileSource(file);
        JsonFactory factory = new JsonFactory()
                .configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true)
                .configure(JsonParser.Feature.);
        this.reader = factory.createParser(new FileReader(file.toFile()));
    }

    void parse() throws IOException {
        JsonToken token;
        token = reader.nextToken(); System.out.println(token + " " + reader.currentLocation().getCharOffset());
        token = reader.nextToken(); System.out.println(token + " " + reader.currentLocation().getCharOffset());
        token = reader.nextToken(); System.out.println(token + " " + reader.currentLocation().getCharOffset());
        token = reader.nextToken(); System.out.println(token + " " + reader.currentLocation().getCharOffset());
        token = reader.nextToken(); System.out.println(token + " " + reader.currentLocation().getCharOffset());

        System.out.println(token);
        System.out.println(reader.getValueAsString());
        System.out.println(reader.currentLocation().getCharOffset());

    }

    public static void main(String[] args) throws IOException {
        String json = "/home/vk/repository/RSVP/rsvp/policy-dl/src/test/resources/translation/common-type/entities.json";
        JacksonEntityReader reader = new JacksonEntityReader(Path.of(json));
        reader.parse();
    }

}
