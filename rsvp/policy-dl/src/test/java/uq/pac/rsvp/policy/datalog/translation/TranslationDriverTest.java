package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.EntityType;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.attribute.AttributeType;
import uq.pac.rsvp.policy.ast.schema.attribute.EntityOrCommonType;
import uq.pac.rsvp.policy.ast.schema.attribute.PrimitiveType;
import uq.pac.rsvp.policy.ast.schema.attribute.SetType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationDriverTest {

    private static final Path ENTITIES = Path.of("examples/photoapp/entities.json");
    private static final Path POLICIES = Path.of("examples/photoapp/policy.cedar");

    Schema getSchema() {
        EntityType visibility = new EntityType(Collections.emptySet(), Collections.emptyMap());
        EntityType role = new EntityType(Collections.emptySet(), Collections.emptyMap());

        AttributeType accountName = new PrimitiveType(PrimitiveType.Type.String);
        AttributeType accountAge = new PrimitiveType(PrimitiveType.Type.Long);
        AttributeType accountRole = new EntityOrCommonType("Role");
        AttributeType accountFriend = new EntityOrCommonType("Account");
        AttributeType accountFriends = new SetType(accountFriend);
        Map<String, AttributeType> accountAttrs = Map.of("name", accountName,
                "age", accountAge,
                "role", accountRole,
                "friends", accountFriends);
        EntityType account = new EntityType(Collections.emptySet(), accountAttrs);

        AttributeType photoAlbum = new EntityOrCommonType("Album");
        AttributeType photoSize = new PrimitiveType(PrimitiveType.Type.Long);
        AttributeType photoType = new PrimitiveType(PrimitiveType.Type.String);
        Map<String, AttributeType> photoAttrs = Map.of("type", photoType,
                "size", photoSize,
                "album", photoAlbum);
        EntityType photo = new EntityType(Collections.emptySet(), photoAttrs);

        AttributeType albumOwner = new EntityOrCommonType("Account");
        AttributeType albumVisibility = new EntityOrCommonType("Visibility");
        Map<String, AttributeType> albumAttrs = Map.of("owner", albumOwner,
                "visibility", albumVisibility);
        EntityType album = new EntityType(Collections.emptySet(), albumAttrs);

        Map<String, EntityType> entityTypes = Map.of(
                "Visibility", visibility,
                "Role", role,
                "Account", account,
                "Photo", photo,
                "Album", album
        );

        // FIXME: this should not be here
        visibility.setName("Visibility");
        role.setName("Role");
        account.setName("Account");
        photo.setName("Photo");
        album.setName("Album");

        Namespace namespace = new Namespace(entityTypes, Collections.emptyMap(), Collections.emptyMap());
        namespace.setName("");
        Schema schema = new Schema();
        schema.put("", namespace);
        return schema;
    }

    TypeInfo getTypeInfo() {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.put("principal", new EntityOrCommonType("Account"));
        typeInfo.put("action", new EntityOrCommonType("Action"));
        typeInfo.put("resource", new EntityOrCommonType("Photo"));
        return typeInfo;
    }

    // Generating the following expression
    //      principal: principal is Account &&
    //      action:    action == Action::"viewPhoto" &&
    //      resource:  resource is Photo &&
    //      when:      resource.album.visibility == Visibility::"Public"
    private Map<String, Expression> makeExpressions() {
        Map<String, Expression> data = new HashMap<>();

        VariableExpression principal = new VariableExpression(VariableExpression.Reference.Principal);
        VariableExpression action = new VariableExpression(VariableExpression.Reference.Action);
        VariableExpression resource = new VariableExpression(VariableExpression.Reference.Resource);
        EntityExpression viewPhoto = new EntityExpression("viewPhoto", List.of("Action"));
        EntityExpression publicVisibility = new EntityExpression("Public", List.of("Visibility"));
        StringExpression photo = new StringExpression("Photo");
        StringExpression account = new StringExpression("Account");
        PropertyAccessExpression prop1 = new PropertyAccessExpression(resource, "album");
        PropertyAccessExpression prop2 = new PropertyAccessExpression(prop1, "visibility");

        // principal is Account
        data.put("principal", new BinaryExpression(principal, BinaryExpression.BinaryOp.Is, account));
        // action == Action::viewPhoto
        data.put("action", new BinaryExpression(action, BinaryExpression.BinaryOp.Eq, viewPhoto));
        // resource is Photo
        data.put("resource", new BinaryExpression(resource, BinaryExpression.BinaryOp.Is, photo));
        // resource.album.visibility == Visibility::"Public"
        data.put("when", new BinaryExpression(prop2, BinaryExpression.BinaryOp.Eq, publicVisibility));

        return data;
    }

    @Test
    public void test() throws IOException {
        Entities entities = Entities.parse(ENTITIES);
        Schema schema = getSchema();
        TranslationSchema translationSchema = TranslationSchema.get(schema);

        TypeInfo types = getTypeInfo();
        Map<String, Expression> expressions = makeExpressions();

        BinaryExpression e = (BinaryExpression) expressions.get("when");
        TranslationPropertyAccessVisitor vs = new TranslationPropertyAccessVisitor(translationSchema, types);
        String var = e.getLeft().compute(vs);
        System.out.println(var);
        System.out.println(vs.getExpressions());
    }
}
