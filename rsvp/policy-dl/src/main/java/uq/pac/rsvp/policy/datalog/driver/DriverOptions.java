package uq.pac.rsvp.policy.datalog.driver;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

public class DriverOptions extends OptionsBase {
    @Option(name = "help",
            abbrev = 'h',
            help = "Prints usage info.",
            defaultValue = "false")
    public boolean help;

    @Option(name = "policies",
            abbrev = 'p',
            help = "Cedar policy file.",
            category = "input",
            defaultValue = "null")
    public String policyFile;

    @Option(name = "schema",
            abbrev = 's',
            help = "Cedar schema file.",
            category = "input",
            defaultValue = "null")
    public String schemaFile;

    @Option(name = "entities",
            abbrev = 'e',
            help = "Cedar entities",
            category = "input",
            defaultValue = "null")
    public String entitiesFile;

    @Option(name = "datalog-dir",
            abbrev = 'D',
            help = "A directory with Datalog outputs",
            category = "input",
            defaultValue = "null")
    public String datalogDir;

    @Option(name = "validate",
            abbrev = 'v',
            help = "Validate against cedar",
            category = "input",
            defaultValue = "false")
    public boolean validate;
}
