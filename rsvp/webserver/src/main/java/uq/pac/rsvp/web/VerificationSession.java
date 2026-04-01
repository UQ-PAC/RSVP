package uq.pac.rsvp.web;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class VerificationSession {

    private Map<String, Path> files;

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

}
