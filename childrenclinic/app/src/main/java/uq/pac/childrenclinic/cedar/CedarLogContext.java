/*
 * Copyright 2026 Gabriel Henrique Lopes Gomes Alves Nunes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
