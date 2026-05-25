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

package uq.pac.childrenclinic.guardian;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class GuardianFormatter implements Formatter<Guardian> {

	private final GuardianRepository guardians;

	public GuardianFormatter(GuardianRepository guardians) {
		this.guardians = guardians;
	}

	@Override
	public String print(Guardian guardian, Locale locale) {
		if (guardian == null || guardian.getId() == null) {
			return "";
		}
		return guardian.getId().toString();
	}

	@Override
	public Guardian parse(String text, Locale locale) throws ParseException {
		if (text == null || text.trim().isEmpty()) {
			throw new ParseException("Guardian identifier cannot be empty or null.", 0);
		}

		try {
			Integer id = Integer.valueOf(text);
			return this.guardians.findById(id)
				.orElseThrow(() -> new ParseException(
						"Guardian entity not found in persistence context for identifier: " + text, 0));
		}
		catch (NumberFormatException exception) {
			throw new ParseException("Submitted Guardian identifier violates numerical formatting constraints: " + text,
					0);
		}
	}

}
