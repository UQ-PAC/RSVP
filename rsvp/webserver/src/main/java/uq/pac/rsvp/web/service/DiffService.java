package uq.pac.rsvp.web.service;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class DiffService {

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
