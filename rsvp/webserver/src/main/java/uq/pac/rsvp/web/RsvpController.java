package uq.pac.rsvp.web;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import uq.pac.rsvp.RsvpException;
import uq.pac.rsvp.support.reporting.Report;

@RestController
@SpringBootApplication
public class RsvpController {

    Logger logger = LoggerFactory.getLogger(RsvpController.class);

    @Autowired
    FileService fileService;

    @Autowired
    VerificationService verificationService;

    @Autowired
    DiffService diffService;

    @PostMapping(path = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String upload(@RequestPart MultipartFile file) throws IOException {
        logger.info("POST /upload ({})", file.getOriginalFilename());
        return fileService.createTempFile(file);

    }

    @GetMapping("/file/{id}")
    public String getFile(@PathVariable String id) throws IOException {
        logger.info("GET /file/{}", id);
        return fileService.readFile(id);
    }

    @DeleteMapping("/file/{id}")
    public String deleteFile(@PathVariable String id) throws IOException {
        logger.info("DELETE /file/{}", id);
        return fileService.deleteFile(id);
    }

    @PostMapping("/verify")
    public Set<Report> verify(
            @Validated @RequestBody VerificationFileset verification)
            throws RsvpException, IOException {
        logger.info("POST /verify");
        return verificationService.runVerification(verification);
    }

    @GetMapping("/diff")
    public String diff(@RequestParam String a, @RequestParam String b) throws IOException {
        logger.info("GET /diff ? {} & {}", a, b);
        return diffService.getDiff(a, b);
    }

    public static void main(String[] args) {
        SpringApplication.run(RsvpController.class, args);
    }

}
