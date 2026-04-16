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

    public String getDiff(String a, String b) throws IOException {

        List<String> one = fileService.getLines(a);
        List<String> two = fileService.getLines(b);

        Patch<String> patch = DiffUtils.diff(one, two);
        List<String> diff = UnifiedDiffUtils.generateUnifiedDiff(a, b, one, patch, 2);

        return String.join("\n", diff);
    }

}
