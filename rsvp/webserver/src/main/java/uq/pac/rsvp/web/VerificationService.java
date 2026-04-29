package uq.pac.rsvp.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.verification.ConfigurationException;
import uq.pac.rsvp.verification.Verification;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    @Autowired
    FileService fileService;

    public Set<Report> runVerification(VerificationFileset verification)
            throws RsvpException, IOException, InterruptedException {

        Map<String, String> filenames = new HashMap<>();

        Set<List<Path>> policies = new HashSet<>();
        Set<Path> schemas = new HashSet<>();
        Set<Path> entities = new HashSet<>();
        Set<Path> invariants = new HashSet<>();

        for (List<String> versionedPolicy : verification.getPolicyFiles()) {
            List<Path> group = new ArrayList<>();

            for (String policy : versionedPolicy) {
                Path path = fileService.getPath(policy);
                group.add(path);
                filenames.put(path.toString(), policy);
            }

            policies.add(group);

        }

        for (String schema : verification.getSchemas()) {
            Path path = fileService.getPath(schema);
            schemas.add(path);
            filenames.put(path.toString(), schema);
        }

        for (String entitiesFile : verification.getEntities()) {
            Path path = fileService.getPath(entitiesFile);
            entities.add(path);
            filenames.put(path.toString(), entitiesFile);
        }

        for (String invariantsFile : verification.getInvariants()) {
            Path path = fileService.getPath(invariantsFile);
            invariants.add(path);
            filenames.put(path.toString(), invariantsFile);
        }

        try {

            Set<Report> result = Verification.verifyPolicies(policies, schemas, entities, invariants);

            logger.info("Generated {} reports", result.size());

            return result.stream().map(report -> report.remap(filenames)).collect(Collectors.toSet());
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST);
        }

    }
}
