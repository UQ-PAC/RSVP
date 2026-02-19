package uq.pac.rsvp.policy.datalog.translation;

import uq.pac.rsvp.policy.datalog.ast.DLStatement;

import java.util.List;

public abstract class TranslationComponent {
    public abstract List<DLStatement> getTranslation();
}
