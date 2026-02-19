package uq.pac.rsvp.datalog.translation;

import uq.pac.rsvp.datalog.ast.DLStatement;

import java.util.List;

public abstract class TranslationComponent {
    public abstract List<DLStatement> getTranslation();
}
