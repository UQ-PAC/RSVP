package uq.pac.rsvp.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
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
import uq.pac.rsvp.Verification;
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.web.VerificationFileset.VersionedPolicy;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    @Autowired
    VerificationSession session;

    public String createTempFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        int ext = filename.lastIndexOf('.');

        Path tmp = Files.createTempFile(filename.substring(0, ext), ext > -1 ? filename.substring(ext) : null);
        file.transferTo(tmp);

        String hash = Hashing.sha256()
                .hashString(tmp.toString(), StandardCharsets.UTF_8)
                .toString();

        session.addFile(hash, tmp);
        logger.info("Created: {} ({})", tmp.toString(), hash);

        return hash;
    }

    public String readFile(String id) throws IOException {
        Path file = session.getFile(id);

        logger.info("Reading file with id {}", id);

        if (file == null) {
            logger.error(session.getHashes().toString());
            logger.error(this.toString());
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }

        return Files.readString(file);
    }

    public String deleteFile(String id) throws IOException {
        Path file = session.removeFile(id);
        logger.info("Deleting file with id {}", id);

        if (file == null) {
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }

        Files.delete(file);

        return "";
    }

    public Set<Report> runVerification(VerificationFileset verification)
            throws RsvpException, IOException {

        Set<Report> result = new HashSet<>();

        // for (VerificationFileset verification : verifications) {
        Set<List<VersionedPolicy>> versionedPolicies = verification.getPolicyFiles();

        Set<String> schemas = verification.getSchemas();
        if (schemas.isEmpty()) {
            logger.error("Bad request: no schema included");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST);
        }
        if (schemas.size() > 1) {
            logger.error("Bad request: too many schema files (more than 1)");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST);
        }

        Path schemaFile = session.getFile(schemas.iterator().next());
        logger.info("Schema: {}", schemaFile);

        Set<String> entities = verification.getEntities();
        if (entities.isEmpty()) {
            logger.error("Bad request: no entities included");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST);
        }
        if (entities.size() > 1) {
            logger.error("Bad request: too many entity files (more than 1)");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST);
        }

        Path entitiesFile = session.getFile(entities.iterator().next());

        Set<String> invariants = verification.getInvariants();
        if (invariants.size() > 1) {
            logger.error("Bad request: too many invariant files (more than 1)");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST);
        }

        Path invariantsFile = null;
        String invariantsFileId = null;

        if (!invariants.isEmpty()) {
            invariantsFileId = invariants.iterator().next();
            invariantsFile = session.getFile(invariantsFileId);
        }

        for (List<VersionedPolicy> policy : versionedPolicies) {
            if (policy.size() > 0) {
                // TODO: enable multiple files parsed to single policy set
                // TODO: handle multiple versions
                String fileId = policy.get(0).getId();
                Path file = session.getFile(policy.get(0).getId());

                if (!file.toString().endsWith(".cedar")) {
                    logger.error("Bad request: {} is not a Cedar policy file.", file.toString());
                    throw new ErrorResponseException(HttpStatus.BAD_REQUEST);
                }

                Set<Report> all = Verification.verifyPolicies(fileId, file, schemaFile,
                        entitiesFile, invariantsFileId, invariantsFile);

                // logger.info(all.toString());

                result.addAll(all);
            }
        }

        logger.info("Generated " + result.size() + " reports");

        return result;
    }
}
