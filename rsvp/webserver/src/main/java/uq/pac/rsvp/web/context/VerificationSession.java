/**
 * Copyright (C) 2026 University of Queensland. All rights reserved.
 * Licensed under the Apache License, Version 2.0.
*/

package uq.pac.rsvp.web.context;

import org.springframework.stereotype.Component;
import uq.pac.rsvp.verification.VerificationResult;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
//@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VerificationSession {

    private final Map<String, Path> files;
    private CompletableFuture<VerificationResult> result;

    public VerificationSession() {
        files = new HashMap<>();
    }

    public synchronized void addFile(String hash, Path file) {
        files.put(hash, file);
    }

    public Path getFile(String hash) {
        return files.get(hash);
    }

    public synchronized Path removeFile(String hash) {
        return files.remove(hash);
    }

    public Set<String> getHashes() {
        return Set.copyOf(files.keySet());
    }

    public CompletableFuture<VerificationResult> getResult() {
        return result;
    }

    public synchronized void setResult(CompletableFuture<VerificationResult> value) {
        result = value;
    }

}
