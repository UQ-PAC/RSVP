package uq.pac.rsvp.policy.ast.schema;

import org.junit.jupiter.api.Test;
import uq.pac.rsvp.policy.ast.antlrschema.AntlrSchema;
import uq.pac.rsvp.policy.ast.antlrschema.parser.AntlrSchemaParser;

public class SchemaDifferentialTest {

    @Test
    void test() {
        AntlrSchema schema = AntlrSchemaParser.parse("text.txt", SCHEMA_TEXT);
        System.out.println(schema);
    }

    private final static String SCHEMA_TEXT = """
            namespace HealthCareApp {
                entity Role enum ["Admin", "Doctor", "InsuranceRep"];
                entity User in [Role];
                entity InfoType;
                entity Info in [InfoType] {
                    provider: User,
                    patient: User
                };
                action createAppointment in [updateAppointment] appliesTo {
                    principal: [User],
                    resource: [Info],
                    context: {
                        referrer: User,
                        detail: AppointmentDetails
                    }
                };
                action updateAppointment appliesTo {
                    principal: [User],
                    resource: [Info],
                };
                action deleteAppointment appliesTo {
                    principal: [User],
                    resource: [Info],
                };
                action listAppointments appliesTo {
                    principal: [User],
                    resource: [Info],
                };
                type AppointmentDetails = {
                    reason: String,
                    cost: Long,
                };
                type Diagnosis = String;
            }
            """;
}
