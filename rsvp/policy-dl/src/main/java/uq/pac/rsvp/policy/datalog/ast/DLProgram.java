package uq.pac.rsvp.policy.datalog.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DLProgram extends DLNode {
    private final List<DLStatement> statements;

    public DLProgram(List<DLStatement> statements) {
        this.statements = statements.stream().toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String stringify() {
        return String.join("\n", statements.stream().map(DLStatement::toString).toList());
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DLProgram p) {
            return p.statements.equals(statements);
        }
        return false;
    }

    public static class Builder {
        private final List<DLStatement> statements;

        public Builder() {
            this.statements = new ArrayList<DLStatement>();
        }

        public Builder add(DLStatement stmt) {
            statements.add(stmt);
            return this;
        }

        public Builder comment(String msg) {
            add(new DLComment(msg));
            return this;
        }

        public Builder add(List<DLStatement> stmts) {
            statements.addAll(stmts);
            return this;
        }

        public Builder space() {
            return comment("");
        }

        public Builder fact(String relation, DLTerm ...terms) {
            return add(new DLFact(new DLAtom(relation, Arrays.stream(terms).toList())));
        }

        public DLProgram build() {
            return new DLProgram(statements);
        }

        public Builder reset() {
            statements.clear();
            return this;
        }

    }
}
