package uq.pac.rsvp.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.google.common.hash.Hashing;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.Verification;
import uq.pac.rsvp.support.reporting.Report;

@RestController
@SpringBootApplication
public class RsvpController {

    Logger logger = LoggerFactory.getLogger(RsvpController.class);

    @Autowired
    VerificationSession session;

    @PostMapping(path = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String upload(@RequestPart MultipartFile document) throws IOException {
        logger.info("POST /upload (" + document.getOriginalFilename() + ")");

        String filename = document.getOriginalFilename();
        int ext = filename.lastIndexOf('.');

        Path tmp = Files.createTempFile(filename.substring(0, ext), ext > -1 ? filename.substring(ext) : null);
        document.transferTo(tmp);

        logger.info("Created: " + tmp.toString());

        String hash = Hashing.sha256()
                .hashString(tmp.toString(), StandardCharsets.UTF_8)
                .toString();

        session.addFile(hash, tmp);

        return hash;
    }

    // TODO: how to prevent?
    @DeleteMapping("/upload")
    public String delete() {
        logger.info("DELETE /upload");
        return new String();
    }

    @GetMapping("/file/{id}")
    public String getFile(@PathVariable String id) throws IOException {
        logger.info("GET /file/" + id);

        Path file = session.getFile(id);

        if (file == null) {
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }

        return Files.readString(file);
    }

    @DeleteMapping("/file/{id}")
    public String deleteFile(@PathVariable String id) {
        logger.info("DELETE /file/" + id);

        Path file = session.removeFile(id);

        if (file == null) {
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }

        return new String();
    }

    @GetMapping("/reports")
    public Set<Report> verify() throws RsvpException, IOException {
        logger.info("GET /reports");

        Set<Report> result = new HashSet<>();

        for (String hash : session.getHashes()) {
            Path file = session.getFile(hash);
            // logger.info(file.toString());

            if (file.toString().endsWith(".cedar")) {
                logger.info("Verifying: " + file);

                List<Report> all = Verification.verifyPolicies(hash, Files.readString(file));
                logger.info(all.toString());

                result.addAll(all);

                break;
            }
        }

        logger.info("Generated " + result.size() + " reports");

        return result;
    }

    public static void main(String[] args) {
        SpringApplication.run(RsvpController.class, args);
    }

}
