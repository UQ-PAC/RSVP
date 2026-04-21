package uq.pac.rsvp.policy.datalog.invariant;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import uq.pac.rsvp.policy.ast.expr.*;
import uq.pac.rsvp.policy.datalog.translation.TranslationError;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.LineLoc;
import uq.pac.rsvp.support.SourceLoc;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override public Expression visitInvariant(CedarParser.InvariantContext ctx) {
        return visitChildren(ctx);
    }

    @Override public Expression visitPolicy(CedarParser.PolicyContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Expression visitProgram(CedarParser.ProgramContext ctx) {
        throw new TranslationError("Program in expression visitor");
    }
}
