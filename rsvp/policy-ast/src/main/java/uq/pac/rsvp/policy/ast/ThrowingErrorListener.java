package uq.pac.rsvp.policy.ast;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.LineLoc;
import uq.pac.rsvp.support.SourceLoc;

public class ThrowingErrorListener extends BaseErrorListener {
    private final FileSource fs;

    public ThrowingErrorListener(FileSource fs) {
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

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object sym, int line, int pos,
                            String msg, RecognitionException e) {
        SourceLoc loc;
        if (e != null && e.getOffendingToken() != null) {
            loc = location(e.getOffendingToken());
        } else {
            int offset = fs.getPosition(line, pos == 0 ? 1 : pos) - 1;
            loc = fs.getSourceLoc(offset, 1);
        }
        throw new ParseError("Parse Error: " + msg, loc);
    }
}
