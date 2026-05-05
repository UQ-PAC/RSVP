package uq.pac.childrenclinic.cedar;

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
		List<String> sortedLogs = new ArrayList<>(this.logs);

		sortedLogs.sort((log1, log2) -> {
			boolean isPage1 = log1 != null && log1.startsWith("Page Request");
			boolean isPage2 = log2 != null && log2.startsWith("Page Request");

			if (isPage1 && !isPage2) {
				return -1;
			}
			else if (!isPage1 && isPage2) {
				return 1;
			}
			return 0;
		});

		return sortedLogs;
	}

	public void clearLogs() {
		this.logs.clear();
	}

}
