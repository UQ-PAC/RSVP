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

package uq.pac.childrenclinic.doctor;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class SpecialtyFormatter implements Formatter<Specialty> {

	private final SpecialtyRepository specialties;

	public SpecialtyFormatter(SpecialtyRepository specialties) {
		this.specialties = specialties;
	}

	@Override
	public String print(Specialty specialty, Locale locale) {
		return specialty.getName() != null ? specialty.getName() : "";
	}

	@Override
	public Specialty parse(String text, Locale locale) throws ParseException {
		return this.specialties.findAll()
			.stream()
			.filter(s -> s.getName().equals(text))
			.findFirst()
			.orElseThrow(() -> new ParseException("Specialty not found: " + text, 0));
	}

}
