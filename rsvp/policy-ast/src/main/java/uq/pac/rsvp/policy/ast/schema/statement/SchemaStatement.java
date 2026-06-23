/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema.statement;

import uq.pac.rsvp.policy.ast.schema.SchemaAstNode;
import uq.pac.rsvp.policy.ast.schema.type.TypeReference;
import uq.pac.rsvp.support.SourceLoc;

public abstract class SchemaStatement extends SchemaAstNode {

    private final TypeReference reference;
    private final Annotations annotations;

    public SchemaStatement(TypeReference reference, Annotations annotations, SourceLoc location) {
        super(location);
        this.reference = reference;
        this.annotations = annotations;
    }

    public TypeReference getTypeReference() {
        return reference;
    }

    public String getName() {
        return reference.getName();
    }

    public String getNamespace() {
        return reference.getNamespace();
    }

    public String getBaseName() {
        return reference.getBaseName();
    }

    public Annotations getAnnotations() {
        return annotations;
    }
}
