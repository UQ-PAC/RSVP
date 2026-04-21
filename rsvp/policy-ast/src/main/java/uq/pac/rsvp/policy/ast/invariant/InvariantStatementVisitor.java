package uq.pac.rsvp.policy.ast.invariant;

import org.antlr.v4.runtime.ParserRuleContext;
import uq.pac.rsvp.policy.ast.CedarBaseVisitor;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.LineLoc;
import uq.pac.rsvp.support.SourceLoc;


class InvariantStatementVisitor extends CedarBaseVisitor<Expression> {

    private final FileSource fs;

    public InvariantStatementVisitor(FileSource fs) {
        this.fs = fs;
    }

    private SourceLoc location(ParserRuleContext context) {
        int startLine = context.start.getLine(),
                startColumn = context.start.getCharPositionInLine() + 1,
                endLine = context.stop.getLine(),
                endColumn = context.stop.getCharPositionInLine() + context.stop.getText().length();
        return fs.getSourceLoc(
                new LineLoc(startLine, startColumn),
                new LineLoc(endLine, endColumn));
    }
}
