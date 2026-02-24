package uq.pac.rsvp.policy.datalog.translation;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.InternalException;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.ast.schema.Namespace;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.datalog.ast.DLAtom;
import uq.pac.rsvp.policy.datalog.ast.DLRule;
import uq.pac.rsvp.policy.datalog.ast.DLTerm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class TranslationDriverTest {

    private static final Path ENTITIES = Path.of("examples/photoapp/entities.json");
    private static final Path POLICIES = Path.of("examples/photoapp/policy.cedar");
    private static final Path SCHEMA = Path.of("examples/photoapp/schema.cedarschema");

    Schema getSchema() throws IOException, InternalException {
        return Schema.parseCedarSchema(SCHEMA);
    }

    TranslationTypeInfo getTypeInfo(Schema schema) {
        Namespace ns = schema.get("");
        TranslationTypeInfo typeInfo = new TranslationTypeInfo();
        typeInfo.put("principal", new EntityTypeReference(ns.getEntityType("Account")));
        typeInfo.put("action", new EntityTypeReference(ns.getEntityType("Action")));
        typeInfo.put("resource", new EntityTypeReference(ns.getEntityType("Photo")));
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
    public void test() throws IOException, InternalException {
        Entities entities = Entities.parse(ENTITIES);
        Schema schema = getSchema();
        TranslationSchema translationSchema = TranslationSchema.get(schema);

        TranslationTypeInfo types = getTypeInfo(schema);
        Map<String, Expression> expressions = makeExpressions();

        Expression principal = expressions.get("principal");
        Expression resource = expressions.get("resource");
        Expression action = expressions.get("action");
        Expression when = expressions.get("when");

        BinaryExpression p1 = new BinaryExpression(
                principal, BinaryExpression.BinaryOp.And, resource);
        BinaryExpression p2 = new BinaryExpression(
                p1, BinaryExpression.BinaryOp.And, action);
        BinaryExpression policy = new BinaryExpression(
                p2, BinaryExpression.BinaryOp.And, when);

        TranslationVisitor et = new TranslationVisitor(translationSchema, types);
        policy.accept(et);

        DLAtom atom = new DLAtom("permit",
                DLTerm.var("principal"), DLTerm.var("resource"), DLTerm.var("action"));
        DLRule rule = new DLRule(atom, et.expressions);
        System.out.println(rule);
    }
}
