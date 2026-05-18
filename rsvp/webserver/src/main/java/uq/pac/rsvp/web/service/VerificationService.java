package uq.pac.rsvp.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uq.pac.rsvp.verification.ConfigurationException;
import uq.pac.rsvp.verification.Verification;
import uq.pac.rsvp.verification.VerificationCache;
import uq.pac.rsvp.verification.VerificationResult;
import uq.pac.rsvp.verification.impact.ChangeImpact;
import uq.pac.rsvp.web.VerificationRequestFileset;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    @Autowired
    FileService fileService;

    public VerificationResult runVerification(VerificationRequestFileset fileset) {

        try {
            VerificationResult result = Verification.verifyPolicies(fileset.resolve(fileService));

            logger.info("Generated {} reports", result.reports().size());

            return result;
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public ChangeImpact getImpact(String original, String updated, VerificationCache cache) {
        try {
            return Verification.getImpact(original, updated, cache);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
