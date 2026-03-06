package uq.pac.rsvp.policy.datalog.driver;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

public class DriverOptions extends OptionsBase {
    @Option(
            name = "help",
            abbrev = 'h',
            help = "Prints usage info.",
            defaultValue = "true")
    public boolean help;

    @Option(
            name = "policies",
            abbrev = 'p',
            help = "Cedar policy file.",
            category = "input",
            defaultValue = "null")
    public String policyFile;

    @Option(
            name = "schema",
            abbrev = 's',
            help = "Cedar schema file.",
            category = "input",
            defaultValue = "null")
    public String schemaFile;

    @Option(
            name = "entities",
            abbrev = 'e',
            help = "Cedar entities",
            category = "input",
            defaultValue = "null")
    public String entitiesFile;

    @Option(
            name = "requests",
            abbrev = 'r',
            help = "Authorisation requests",
            category = "input",
            defaultValue = "null")
    public String queriesFile;

    @Option(
            name = "datalog-dir",
            abbrev = 'D',
            help = "A directory with Datalog outputs",
            category = "input",
            defaultValue = "rsvp-tmp")
    public String datalogDir;
}
