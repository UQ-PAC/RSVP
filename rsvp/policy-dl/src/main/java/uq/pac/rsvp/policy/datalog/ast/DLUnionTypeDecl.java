package uq.pac.rsvp.policy.datalog.ast;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class DLUnionTypeDecl extends DLTypeDecl {
    private final DLType decl;
    private final Set<DLType> components;

    public DLUnionTypeDecl(DLType decl, Collection<DLType> components) {
        this.decl = decl;
        this.components = components.stream().collect(Collectors.toUnmodifiableSet());
    }

    public DLType getDeclared() {
        return decl;
    }

    public Set<DLType> getComponents() {
        return components;
    }

    @Override
    public String stringify() {
        return ".type " + decl + " = " +
                String.join(" | ", components.stream().map(DLType::toString).toList());
    }
}
