package uq.pac.rsvp.web.service;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uq.pac.rsvp.verification.Verification;
import uq.pac.rsvp.verification.VerificationCache;
import uq.pac.rsvp.verification.impact.RequestStatus;

import java.io.IOException;
import java.util.List;

@Service
public class DiffService {
    private static final Logger logger = LoggerFactory.getLogger(DiffService.class);

    @Autowired
    FileService fileService;

    public String getDiff(String original, String originalName, String updated, String updatedName) throws IOException {

        List<String> one = fileService.getLines(original);
        List<String> two = fileService.getLines(updated);

        Patch<String> patch = DiffUtils.diff(one, two);
        List<String> diff = UnifiedDiffUtils.generateUnifiedDiff(originalName, updatedName, one, patch, 4);

        return String.join("\n", diff);
    }

    public String getImpact(String original, String updated, VerificationCache cache) {
        try {
            List<RequestStatus> impact = Verification.getImpact(original, updated, cache);

            List<String> permitted = impact.stream().filter(RequestStatus::isPermitted).map(RequestStatus::getRequest).toList();
            List<String> forbidden = impact.stream().filter(RequestStatus::isForbidden).map(RequestStatus::getRequest).toList();

            Patch<String> patch = DiffUtils.diff(forbidden, permitted);
            List<String> diff = UnifiedDiffUtils.generateUnifiedDiff(original, updated, permitted, patch, 0);

            return String.join("\n", diff);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
