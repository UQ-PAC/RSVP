package policygen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.policy.ast.schema.ActionDefinition;
import uq.pac.rsvp.policy.ast.schema.CommonTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.EntityTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.Schema;
import uq.pac.rsvp.policy.ast.schema.common.BooleanType;
import uq.pac.rsvp.policy.ast.schema.common.CommonTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.EntityTypeReference;
import uq.pac.rsvp.policy.ast.schema.common.LongType;
import uq.pac.rsvp.policy.ast.schema.common.RecordTypeDefinition;
import uq.pac.rsvp.policy.ast.schema.common.StringType;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitor;
import uq.pac.rsvp.policy.ast.visitor.SchemaVisitorImpl;

public class SchemaLoader {

    public static SchemaWrapper load(String path) throws RsvpException {
        Path schemaPath = Path.of(path);
        Schema theSchema = Schema.parseCedarSchema(schemaPath);

        SchemaWrapper[] resultSchemaWrapper = new SchemaWrapper[1];
        SchemaVisitor visitor = new SchemaVisitorImpl() {

            Map<EntityTypeDefinition, CedarEntityRef> entityTypes = new HashMap<>();
            List<CedarAction> actions = new ArrayList<>();

            private CedarType mapType(CommonTypeDefinition ctd) {
                CedarType[] resultType = new CedarType[1];
                ctd.accept(new SchemaVisitorImpl() {
                    @Override
                    public void visitBoolean(BooleanType type) {
                        resultType[0] = CedarPrimitive.BOOL;
                    }

                    @Override
                    public void visitLong(LongType type) {
                        resultType[0] = CedarPrimitive.LONG;
                    }

                    @Override
                    public void visitString(StringType type) {
                        resultType[0] = CedarPrimitive.STRING;
                    }

                    @Override
                    public void visitCommonTypeReference(CommonTypeReference type) {
                        // TODO Auto-generated method stub
                        //visitCommonTypeReference(type);
                        System.out.println("*** " + type.getName());
                        if (type.getDefinition() == null) {
                            System.out.println("*** definition is null");
                        }
                        type.getDefinition().accept(this);
                    }

                    @Override
                    public void visitEntityTypeReference(EntityTypeReference type) {
                        String entityTypeName = type.getDefinition().getName();
                        CedarEntityRef entityRef = entityTypes.computeIfAbsent(type.getDefinition(),
                                k -> new CedarEntityRef(entityTypeName));
                        resultType[0] = entityRef;
                    }

                    @Override
                    public void visitRecordTypeDefinition(RecordTypeDefinition rtd) {
                        Set<String> attrNames = rtd.getAttributeNames();
                        CedarField[] fields = new CedarField[attrNames.size()];

                        int i = 0;
                        for (String attrName : attrNames) {
                            fields[i++] = new CedarField(attrName, mapType(rtd.getAttributeType(attrName)));
                        }

                        resultType[0] = new CedarRecord(fields);
                    }
                });

                if (resultType[0] == null)
                    throw new UnsupportedOperationException("Unhandled type in schema: " + ctd);

                return resultType[0];
            }

            @Override
            public void visitEntityTypeDefinition(EntityTypeDefinition etd) {
                // etd.getEntityNamesEnum()
                // etd.getMemberOfTypes()
                String entityTypeName = etd.getName();

                // Create the type now in case of recursive definition (attributes of
                // entity type), or retrieve the existing (empty) entity in case it has
                // been referenced previously:
                CedarEntityRef entityRef = entityTypes.computeIfAbsent(etd,
                        k -> new CedarEntityRef(entityTypeName));

                Set<String> attrNames = etd.getShapeAttributeNames();
                CedarField[] fields = new CedarField[attrNames.size()];

                int i = 0;
                for (String attrName : attrNames) {
                    fields[i++] = new CedarField(attrName, mapType(etd.getShapeAttributeType(attrName)));
                }

                entityRef.setFields(fields);

                Set<EntityTypeDefinition> memberOfETDs = etd.getMemberOfTypes();
                List<CedarEntityRef> memberOfTypes = new ArrayList<>();
                for (EntityTypeDefinition parentETD : memberOfETDs) {
                    CedarEntityRef parentRef = entityTypes.computeIfAbsent(parentETD,
                            k -> new CedarEntityRef(parentETD.getName()));
                    memberOfTypes.add(parentRef);
                }

                entityRef.setParentTypes(memberOfTypes);
            }

            @Override
            public void visitActionDefinition(ActionDefinition action) {

                // Actions:
                // - May apply to a context (record) type
                // - Apply to specific principal type(s)
                // - Apply to specific resource type(s)
                List<CedarEntityRef> principals = new ArrayList<>();
                for (EntityTypeDefinition etd : action.getAppliesToPrincipalTypes()) {
                    CedarEntityRef entityRef = entityTypes.computeIfAbsent(etd,
                            k -> new CedarEntityRef(etd.getName()));
                    principals.add(entityRef);
                }

                List<CedarEntityRef> resources = new ArrayList<>();
                for (EntityTypeDefinition etd : action.getAppliesToResourceTypes()) {
                    CedarEntityRef entityRef = entityTypes.computeIfAbsent(etd,
                            k -> new CedarEntityRef(etd.getName()));
                    resources.add(entityRef);
                }

                CedarRecord context = null;
                if (action.getAppliesToContext() != null) {
                    context = (CedarRecord) mapType(action.getAppliesToContext());
                }

                CedarAction newAction = new CedarAction(action.getQualifiedName(), principals, resources, context);
                actions.add(newAction);
            }

            @Override
            public void visitSchema(Schema schema) {
                super.visitSchema(schema);
                resultSchemaWrapper[0] = new SchemaWrapperImpl(entityTypes.values(), actions);
            }
        };

        visitor.visitSchema(theSchema);
        return resultSchemaWrapper[0];
    }

}
