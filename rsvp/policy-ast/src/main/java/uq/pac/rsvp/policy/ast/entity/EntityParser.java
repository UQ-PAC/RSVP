package uq.pac.rsvp.policy.ast.entity;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import uq.pac.rsvp.policy.ast.JsonLexer;
import uq.pac.rsvp.policy.ast.JsonParser;
import uq.pac.rsvp.policy.ast.ThrowingErrorListener;
import uq.pac.rsvp.support.FileSource;
import uq.pac.rsvp.support.error.SyntaxError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static uq.pac.rsvp.policy.ast.Util.unquote;

/**
 * Parse a JSON specification describing a set of Cedar entities and output then as an {@link EntitySet}
 */
class EntityParser {

    public static EntitySet parse(Path path) throws IOException {
        String file = path.getFileName().toString();
        String text = Files.readString(path);
        return parse(file, text);
    }

    public static EntitySet parse(String file, String text) {
        FileSource fs = new FileSource(file, text);
        ThrowingErrorListener errorListener = new ThrowingErrorListener(fs);

        JsonLexer lexer = new JsonLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JsonParser parser = new JsonParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        EntityValueVisitor valueVisitor = new EntityValueVisitor(fs);
        EntityVisitor entityVisitor = new EntityVisitor(fs, valueVisitor);

        Set<Entity> entities = parser.entitySet().entity().stream().map(e ->
                e.accept(entityVisitor)).collect(Collectors.toSet());
        return new EntitySet(entities);
    }


    static class EntityVisitor extends JsonSourceVisitor<Entity> {
        private final EntityValueVisitor values;

        public EntityVisitor(FileSource fs, EntityValueVisitor values) {
            super(fs);
            this.values = values;
        }

        @Override
        public Entity visitEntity(JsonParser.EntityContext ctx) {
            RecordValue entityRecord = (RecordValue) ctx.object().accept(values);

            EntityReference uid = EntityAttribute.UID.get(entityRecord, EntityReference.class);
            RecordValue attrs = EntityAttribute.ATTRS.get(entityRecord, RecordValue.class);
            EntityValue context = EntityAttribute.CONTEXT.get(entityRecord, EntityValue.class);
            SetValue parentsSet = EntityAttribute.PARENTS.get(entityRecord, SetValue.class);

            Set<EntityReference>  parents = parentsSet.getValues().stream()
                    .map(e -> {
                        if (e instanceof EntityReference ref) {
                            return ref;
                        }
                        throw new SyntaxError("Expected entity reference", e.getSourceLoc());
                    })
                    .collect(Collectors.toSet());

            entityRecord.forEach(((attr, value) -> {
                if (!EntityAttribute.contains(attr.getValue())) {
                    throw new SyntaxError("Unexpected entity key: " + attr, attr.getSourceLoc());
                }
            }));

            return new Entity(uid, attrs, parents, context, entityRecord.getSourceLoc());
        }
    }

    static class EntityValueVisitor extends JsonSourceVisitor<EntityValue> {

        public EntityValueVisitor(FileSource fs) {
            super(fs);
        }

        private static EntityValue getReferenceOrRecord(RecordValue value) {
            AttributeName id = new AttributeName("id"),
                    type = new AttributeName("type");

            if (value.size() == 2 &&
                    value.attributes().equals(Set.of(type, id)) &&
                    value.getValue(type) instanceof StringValue t &&
                    value.getValue(id) instanceof StringValue i) {
                return new EntityReference(t.getValue(), i.getValue(), value.getSourceLoc());
            }
            return value;
        }

        @Override
        public EntityValue visitObject(JsonParser.ObjectContext ctx) {
            Map<AttributeName, EntityValue> values = new HashMap<>();
            ctx.mapping().forEach(mapping -> {
                String key = unquote(mapping.STRING().getText());
                AttributeName attr = new AttributeName(key, location(mapping.STRING().getSymbol()));
                EntityValue value = mapping.value().accept(this);
                values.put(attr, value);
            });

            // FIXME: Need tighter checking here
            RecordValue value = new RecordValue(values, location(ctx));
            AttributeName cedarEntity = new AttributeName("__entity");
            return values.keySet().equals(Set.of(cedarEntity)) ?
                    values.get(cedarEntity) : getReferenceOrRecord(value);
        }

        @Override
        public EntityValue visitObjectExpr(JsonParser.ObjectExprContext ctx) {
            return ctx.object().accept(this);
        }

        @Override
        public EntityValue visitArrayExpr(JsonParser.ArrayExprContext ctx) {
            return ctx.array().accept(this);
        }

        @Override
        public EntityValue visitArray(JsonParser.ArrayContext ctx) {
            Set<EntityValue> values = ctx.value().stream()
                    .map(v -> v.accept(this))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return new SetValue(values, location(ctx));
        }

        @Override
        public EntityValue visitNumberExpr(JsonParser.NumberExprContext ctx) {
            long value = Long.parseLong(ctx.getText());
            return new LongValue(value, location(ctx));
        }

        @Override
        public EntityValue visitBooleanExpr(JsonParser.BooleanExprContext ctx) {
            boolean value = Boolean.parseBoolean(ctx.getText().toLowerCase());
            return new BooleanValue(value, location(ctx));
        }

        @Override
        public EntityValue visitStringExpr(JsonParser.StringExprContext ctx) {
            String value = unquote(ctx.STRING().getText());
            return new StringValue(value, location(ctx));
        }
    }

    private enum EntityAttribute {
        ATTRS("attrs", true),
        UID("uid", true),
        PARENTS("parents", true),
        CONTEXT("context", false);

        private final String name;
        private final boolean required;

        EntityAttribute(String name, boolean required) {
            this.name = name;
            this.required = required;
        }

        private static final Map<Class<?>, String> LABELS = Map.of(
                EntityReference.class, "entity reference",
                RecordValue.class, "record",
                SetValue.class, "set");

        private static final Set<String> ATTRIBUTES = new HashSet<>();
        static {
            for (EntityAttribute attr : EntityAttribute.values()) {
                ATTRIBUTES.add(attr.name);
            }
        }

        public static boolean contains(String attr) {
            return ATTRIBUTES.contains(attr);
        }

        @SuppressWarnings("unchecked")
        <E extends EntityValue> E get(RecordValue entityRecord, Class<E> target) {
            E val = (E) entityRecord.getValue(name);

            if (!required && val == null) {
                return null;
            }
            if (val == null) {
                throw new SyntaxError("Missing " + name + " entity attribute", entityRecord.getSourceLoc());
            }
            if (target.isInstance(val)) {
                return target.cast(val);
            } else {
                throw new SyntaxError("Expected " + LABELS.get(target), val.getSourceLoc());
            }
        }
    }
}
