package uq.pac.rsvp.policy.ast.deserialisation;

import com.google.gson.*;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.PolicySet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PolicySetDeserialiser implements JsonDeserializer<PolicySet> {
    @Override
    public PolicySet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        List<Policy> policies = new ArrayList<>();
        for (JsonObject o : array.asList().stream().map(JsonElement::getAsJsonObject).toList()) {
            policies.add(context.deserialize(o, Policy.class));
        }
        return new PolicySet(policies);
    }
}
