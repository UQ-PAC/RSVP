package uq.pac.rsvp.webapp;


import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.ScopedProxyMode;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, 
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VerificationSession {

    private Map<String, Path> files;

    public VerificationSession() {
        files = new HashMap<>();
    }

    public void addFile(String hash, Path file) {
        files.put(hash, file);
    }

    public Path getFile(String hash) {
        return files.get(hash);
    }

    public Path removeFile(String hash) {
        return files.remove(hash);
    }
    
}
