package uq.pac.rsvp.policy.ast.schema.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import uq.pac.rsvp.policy.ast.CedarschemaBaseVisitor;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.LineLoc;
import uq.pac.rsvp.support.SourceLoc;

class CedarschemaSourceVisitor<T> extends CedarschemaBaseVisitor<T> {
    private final FileSource fs;

    public CedarschemaSourceVisitor(FileSource fs) {
        this.fs = fs;
    }

    protected SourceLoc location(Token start, Token stop) {
        int startLine = start.getLine(),
                startColumn = start.getCharPositionInLine() + 1,
                endLine = stop.getLine(),
                endColumn = stop.getCharPositionInLine() + stop.getText().length();
        return fs.getSourceLoc(
                new LineLoc(startLine, startColumn),
                new LineLoc(endLine, endColumn));
    }

    protected SourceLoc location(Token start) {
        return location(start, start);
    }

    protected SourceLoc location(ParserRuleContext context) {
        return location(context.start, context.stop);
    }
}
