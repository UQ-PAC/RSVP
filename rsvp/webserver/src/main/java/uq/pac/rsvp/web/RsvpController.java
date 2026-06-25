/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
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
import uq.pac.rsvp.support.reporting.Report;
import uq.pac.rsvp.verification.VerificationResult;
import uq.pac.rsvp.verification.impact.ChangeImpact;
import uq.pac.rsvp.web.context.VerificationSession;
import uq.pac.rsvp.web.service.DiffService;
import uq.pac.rsvp.web.service.FileService;
import uq.pac.rsvp.web.service.VerificationService;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


@RestController
@SpringBootApplication
@EnableAsync
public class RsvpController {

    final Logger logger = LoggerFactory.getLogger(RsvpController.class);

    @Autowired
    FileService fileService;

    @Autowired
    VerificationService verificationService;

    @Autowired
    DiffService diffService;

    @Autowired
    VerificationSession session;


    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CompletableFuture<String> upload(@RequestPart MultipartFile file) throws IOException {
        logger.info("POST /upload ({})", file.getOriginalFilename());
        return CompletableFuture.completedFuture(fileService.createTempFile(file));

    }

    @GetMapping("/file/{id}")
    public CompletableFuture<String> getFile(@PathVariable String id) throws IOException {
        logger.info("GET /file/{}", id);
        return CompletableFuture.completedFuture(fileService.readFile(id));
    }

    @DeleteMapping("/file/{id}")
    public CompletableFuture<String> deleteFile(@PathVariable String id) throws IOException {
        logger.info("DELETE /file/{}", id);
        return CompletableFuture.completedFuture(fileService.deleteFile(id));
    }

    @PostMapping("/verify")
    public CompletableFuture<Set<Report>> verify(
            @Validated @RequestBody VerificationRequestFileset fileset) {
        logger.info("POST /verify");

        CompletableFuture<VerificationResult> result = CompletableFuture.supplyAsync(() -> verificationService.runVerification(fileset));
        session.setResult(result);

        return result.thenApply(VerificationResult::reports);
    }

    @GetMapping("/diff")
    public CompletableFuture<String> diff(@RequestParam String original, @RequestParam String originalName, @RequestParam String updated,
                                          @RequestParam String updatedName) throws IOException {
        logger.info("GET /diff ? {} & {}", original, updated);
        return CompletableFuture.completedFuture(diffService.getDiff(original, originalName, updated, updatedName));
    }

    @GetMapping("/impact")
    public CompletableFuture<ChangeImpact> impact(@RequestParam String original, @RequestParam String updated) {
        logger.info("GET /impact ? {} & {}", original, updated);

        // Wait until verification has completed execution before querying impact
        return session.getResult().thenApply(result -> verificationService.getImpact(original, updated, result.cache()));
    }

    public static void main(String[] args) {
        SpringApplication.run(RsvpController.class, args);
    }

}
