package uq.pac.rsvp.policy.ast.expr;

import java.util.regex.Pattern;
import uq.pac.rsvp.policy.ast.policy.PolicyAstNode;
import uq.pac.rsvp.support.SourceLoc;

public abstract class Expression extends PolicyAstNode {

    protected static final Pattern NICE_PROP_NAME = Pattern.compile("[a-zA-Z0-9_]+");

    protected Expression(SourceLoc source) {
        super(source);
    }

}
