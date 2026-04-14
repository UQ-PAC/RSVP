package uq.pac.rsvp.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.hash.Hashing;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

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

    public List<String> getLines(String id) throws IOException {
        return Arrays.asList(getContent(id).split("\n"));
    }

    public String getContent(String id) throws IOException {
        return Files.readString(session.getFile(id));
    }

    public Path getPath(String id) {
        return session.getFile(id);
    }

}
