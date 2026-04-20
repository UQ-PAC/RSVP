package uq.pac.rsvp.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.hash.Hashing;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.verification.Verification;
import uq.pac.rsvp.verification.ConfigurationException;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.support.util.Pair;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    @Autowired
    FileService fileService;

    public Set<Report> runVerification(VerificationFileset verification)
            throws RsvpException, IOException, InterruptedException {

        Set<List<Pair<String, Path>>> policies = new HashSet<>();
        Set<Path> schemas = new HashSet<>();
        Set<Path> entities = new HashSet<>();
        Set<Path> invariants = new HashSet<>();

        for (List<String> versionedPolicy : verification.getPolicyFiles()) {
            List<Pair<String, Path>> group = new ArrayList<>();

            for (String policy : versionedPolicy) {
                group.add(new Pair<String, Path>(policy, fileService.getPath(policy)));
            }

            policies.add(group);

        }

        for (String schema : verification.getSchemas()) {
            schemas.add(fileService.getPath(schema));
        }

        for (String entitiesFile : verification.getEntities()) {
            entities.add(fileService.getPath(entitiesFile));
        }

        for (String invariantsFile : verification.getInvariants()) {
            invariants.add(fileService.getPath(invariantsFile));
        }

        try {

            Set<Report> result = Verification.verifyPolicies(policies, schemas, entities, invariants);
            logger.info("Generated " + result.size() + " reports");

            return result;
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST);
        }

    }
}
