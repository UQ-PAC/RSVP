/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.policy.ast.schema.type;

import uq.pac.rsvp.policy.ast.schema.visitor.SchemaPayloadVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaComputationVisitor;
import uq.pac.rsvp.policy.ast.schema.visitor.SchemaVisitor;
import uq.pac.rsvp.support.SourceLoc;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class RecordType extends BuiltinType {

    public static class Attribute {
        public final String name;
        public final boolean required;

        public Attribute(String name, boolean required) {
            this.name = name;
            this.required = required;
        }

        public Attribute(String name) {
            this(name, true);
        }

        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        @Override
        public String toString() {
            return "\"" + name + "\"" + (required ? "" : "?");
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other == null) {
                return false;
            } else if (other instanceof Attribute attr) {
                return attr.name.equals(this.name);
            }
            return false;
        }
    }

    private final Map<Attribute, BuiltinType> attributes;

    public RecordType(Map<Attribute, BuiltinType> attributes, SourceLoc location) {
        super(location);
        this.attributes = Map.copyOf(attributes);
    }

    public RecordType(Map<Attribute, BuiltinType> attributes) {
        this(attributes, SourceLoc.MISSING);
    }

    public RecordType() {
        this(Collections.emptyMap(), SourceLoc.MISSING);
    }

    public Map<Attribute, BuiltinType> getAttributes() {
        return attributes;
    }

    public BuiltinType getAttribute(String attr) {
        return attributes.get(new Attribute(attr));
    }

    public boolean hasAttribute(String attr) {
        return attributes.containsKey(new Attribute(attr));
    }

    private String toString(BuiltinType type, String indent) {
        if (type instanceof RecordType rec) {
            StringBuilder sb = new StringBuilder();
            if (rec.isEmpty()) {
                sb.append("{ }");
            } else {
                sb.append("{").append("\n");
                // Enforce lexicographical order when outputting attributes
                rec.getAttributes().entrySet().stream()
                        .sorted(Comparator.comparing(a -> a.getKey().toString()))
                        .forEach(a -> {
                            sb.append(indent).append("    ")
                                    .append(a.getKey().toString())
                                    .append(": ")
                                    .append(toString(a.getValue(), indent + "    "))
                                    .append(",\n");
                        });
                sb.append(indent).append("}");
            }
            return sb.toString();
        } else {
            return type.toString();
        }
    }

    @Override
    public String toString() {
        return toString(this, "");
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof RecordType rec) {
            return this.attributes.equals(rec.attributes);
        }
        return false;
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public void accept(SchemaVisitor visitor) {
        visitor.visitRecord(this);
    }

    @Override
    public <T> T compute(SchemaComputationVisitor<T> visitor) {
        return visitor.visitRecord(this);
    }

    @Override
    public <T> void accept(SchemaPayloadVisitor<T> visitor, T payload) {
        visitor.visitRecord(this, payload);
    }
}
