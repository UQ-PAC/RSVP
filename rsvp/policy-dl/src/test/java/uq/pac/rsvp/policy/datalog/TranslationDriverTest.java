package uq.pac.rsvp.policy.datalog;

import com.cedarpolicy.model.entity.Entities;
import com.cedarpolicy.model.exception.AuthException;
import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.Policy;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.ast.DLProgram;
import uq.pac.rsvp.policy.datalog.translation.TranslationDriver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class TranslationDriverTest {

    private static final Path ENTITIES = Path.of("examples/photoapp/entities.json");
    private static final Path POLICIES = Path.of("examples/photoapp/policy.cedar");

    // Generating the following expression
    //      principal is Account &&
    //      action == Action::"viewPhoto" &&
    //      resource is Photo &&
    //      resource.album.visibility == Visibility::"Public"
    private Policy makePolicy() {
        VariableExpression principal = new VariableExpression(VariableExpression.Reference.Principal);
        VariableExpression action = new VariableExpression(VariableExpression.Reference.Action);
        VariableExpression resource = new VariableExpression(VariableExpression.Reference.Resource);
        EntityExpression viewPhoto = new EntityExpression("viewPhoto", List.of("Action"));
        EntityExpression publicVisibility = new EntityExpression("Public", List.of("Visibility"));
        TypeExpression photo = new TypeExpression("Photo");
        TypeExpression account = new TypeExpression("Account");
        PropertyAccessExpression prop1 = new PropertyAccessExpression(principal, "album");
        PropertyAccessExpression prop2 = new PropertyAccessExpression(prop1, "visibility");

        Expression e1 = new BinaryExpression(principal, BinaryExpression.BinaryOp.Is, account);
        Expression e2 = new BinaryExpression(action, BinaryExpression.BinaryOp.Eq, viewPhoto);
        Expression e3 = new BinaryExpression(resource, BinaryExpression.BinaryOp.Is, photo);
        Expression e4 = new BinaryExpression(prop2, BinaryExpression.BinaryOp.Eq, publicVisibility);

        BinaryExpression e5 = new BinaryExpression(e1, BinaryExpression.BinaryOp.And, e2);
        BinaryExpression e6 = new BinaryExpression(e3, BinaryExpression.BinaryOp.And, e4);
        BinaryExpression policyExpression = new BinaryExpression(e5, BinaryExpression.BinaryOp.And, e6);

        return new Policy(Policy.Effect.Permit, policyExpression);
    }

    @Test
    public void test() throws IOException, AuthException {
        Entities entities = Entities.parse(ENTITIES);
        DLProgram program = TranslationDriver.getTranslation(entities);
        Policy p = makePolicy();
        System.out.println(p);
    }
}
