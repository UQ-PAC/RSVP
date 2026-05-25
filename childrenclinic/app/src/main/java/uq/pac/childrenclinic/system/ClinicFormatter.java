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

package uq.pac.childrenclinic.system;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class ClinicFormatter implements Formatter<Clinic> {

	private final ClinicRepository clinics;

	public ClinicFormatter(ClinicRepository clinics) {
		this.clinics = clinics;
	}

	@Override
	public String print(Clinic clinic, Locale locale) {
		String name = clinic.getClinicName();
		return (name != null) ? name : "<null>";
	}

	@Override
	public Clinic parse(String text, Locale locale) throws ParseException {
		Collection<Clinic> findClinics = this.clinics.findClinics();
		for (Clinic clinic : findClinics) {
			if (Objects.equals(clinic.getClinicName(), text)) {
				return clinic;
			}
		}
		throw new ParseException("Clinic not found: " + text, 0);
	}

}
