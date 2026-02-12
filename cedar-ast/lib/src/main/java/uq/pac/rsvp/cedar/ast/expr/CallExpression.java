package uq.pac.rsvp.cedar.ast.expr;

import static uq.pac.rsvp.cedar.ast.expr.Expression.ExprType.Call;

import java.util.List;

import uq.pac.rsvp.cedar.ast.SourceLoc;
import uq.pac.rsvp.cedar.ast.visitor.PolicyVisitor;

public class CallExpression extends Expression {

    private Expression self;
    private String func;
    private List<Expression> args;

    public CallExpression(Expression self, String func, List<Expression> args, SourceLoc source) {
        super(Call, source);
        this.self = self;
        this.func = func;
        this.args = args;
    }

    public CallExpression(String func, List<Expression> args, SourceLoc source) {
        super(Call, source);
        this.func = func;
        this.args = args;
    }

    public Expression getSelf() {
        return self;
    }

    public String getFunc() {
        return func;
    }

    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public void accept(PolicyVisitor visitor) {
        visitor.visitCallExpr(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (self != null) {
            sb.append(self.toString());
            sb.append('.');
        }
        sb.append(func);
        sb.append('(');
        String sep = "";
        for (Expression arg : args) {
            sb.append(sep);
            sb.append(arg.toString());
            sep = ", ";
        }
        sb.append(')');
        return sb.toString();
    }

}
