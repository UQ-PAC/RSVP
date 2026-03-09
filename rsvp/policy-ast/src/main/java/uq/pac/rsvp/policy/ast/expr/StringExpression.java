package uq.pac.rsvp.policy.ast.expr;

import static uq.pac.rsvp.policy.ast.expr.Expression.ExprType.StringLiteral;

import uq.pac.rsvp.support.SourceLoc;
import uq.pac.rsvp.policy.ast.visitor.PolicyComputationVisitor;
import uq.pac.rsvp.policy.ast.visitor.PolicyVisitor;;

public class StringExpression extends Expression {

    private final String value;

    public StringExpression(String value, SourceLoc source) {
        super(StringLiteral, source);
        this.value = value;
    }

    public StringExpression(String value) {
        this(value, SourceLoc.MISSING);
    }

    public StringExpression() {
        this("", SourceLoc.MISSING);
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitStringExpr(this);
    }

    @Override
    public <T> T compute(PolicyComputationVisitor<T> visitor) {
        return visitor.visitStringExpr(this);
    }

    @Override
    public String toString() {
        return "\"" + escape(value) + "\"";
    }

    /**
     * Simple escaping \", \n, \r, \b, \t, \\
     */
    public static String unescape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\') {
                sb.append(c);
            } else {
                i++;
                if (i >= value.length()) {
                    throw new RuntimeException("Invalid escape sequence: \\");
                } else {
                    c = value.charAt(i);
                    char next = switch (c) {
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        case 'b' -> '\b';
                        case '"' -> '"';
                        case '\\' -> '\\';
                        default -> throw new RuntimeException("Invalid escape sequence: \\" + c);
                    };
                    sb.append(next);
                }
            }
        }
        return sb.toString();
    }

    public static String escape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            String next = switch (c) {
                case '\n' -> "\\n";
                case '\r' -> "\\r";
                case '\t' -> "\\t";
                case '\b' -> "\\b";
                case '\"' -> "\\\"";
                case '\\' -> "\\\\";
                default -> Character.toString(c);
            };
            sb.append(next);
        }
        return sb.toString();
    }

}
