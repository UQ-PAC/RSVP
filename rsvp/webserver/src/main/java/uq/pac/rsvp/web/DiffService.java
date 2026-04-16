package uq.pac.rsvp.web;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

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

}
