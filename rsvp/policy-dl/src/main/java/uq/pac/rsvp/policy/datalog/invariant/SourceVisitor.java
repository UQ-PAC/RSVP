package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.ParserRuleContext;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.LineLoc;
import uq.pac.rsvp.support.SourceLoc;

class SourceVisitor<T> extends CedarBaseVisitor<T> {

    private final FileSource fs;

    public SourceVisitor(FileSource fs) {
        this.fs = fs;
    }

    protected SourceLoc location(ParserRuleContext context) {
        int startLine = context.start.getLine(),
                startColumn = context.start.getCharPositionInLine() + 1,
                endLine = context.stop.getLine(),
                endColumn = context.stop.getCharPositionInLine() + context.stop.getText().length();
        return fs.getSourceLoc(
                new LineLoc(startLine, startColumn),
                new LineLoc(endLine, endColumn));
    }
}
