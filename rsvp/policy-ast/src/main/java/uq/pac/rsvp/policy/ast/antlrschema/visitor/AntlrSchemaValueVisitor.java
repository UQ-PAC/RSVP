package uq.pac.rsvp.policy.ast.antlrschema.visitor;

import uq.pac.rsvp.policy.ast.antlrschema.*;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrAction;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrCommonType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrEnumEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.statement.AntlrRecordEntityType;
import uq.pac.rsvp.policy.ast.antlrschema.type.*;

public interface AntlrSchemaValueVisitor<T> {
    T visitSchema(AntlrSchema schema);

    // Statements
    T visitRecordEntity(AntlrRecordEntityType entity);

    T visitEnumEntity(AntlrEnumEntityType entity);

    T visitAction(AntlrAction action);

    T visitCommon(AntlrCommonType type);

    // Compound types
    T visitRecord(AntlrRecordType type);

    T visitSet(AntlrSetType type);

    // Type references
    T visitReference(AntlrTypeReference type);

    // Primitive types
    T visitBoolean(AntlrBooleanType type);

    T visitLong(AntlrLongType type);

    T visitString(AntlrStringType type);
}
