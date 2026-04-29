package uq.pac.childclinic.cedar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CedarLogContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<String> logs = new ArrayList<>();

	public void addLog(String log) {
		this.logs.add(log);
	}

	public List<String> getLogs() {
		return new ArrayList<>(this.logs);
	}

	public void clearLogs() {
		this.logs.clear();
	}

}
