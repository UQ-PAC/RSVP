INSERT INTO clinics VALUES (default, 'Clinic A'); -- 1
INSERT INTO clinics VALUES (default, 'Clinic B'); -- 2

INSERT INTO specialties VALUES (default, 'Radiology'); -- 1
INSERT INTO specialties VALUES (default, 'Surgery');   -- 2
INSERT INTO specialties VALUES (default, 'Dentistry'); -- 3

INSERT INTO roles VALUES (default, 'Administrator'); -- 1
INSERT INTO roles VALUES (default, 'Secretary');     -- 2
INSERT INTO roles VALUES (default, 'Doctor');        -- 3

INSERT INTO levels VALUES (default, 'Intern');     -- 1
INSERT INTO levels VALUES (default, 'Resident');   -- 2
INSERT INTO levels VALUES (default, 'Staff');      -- 3
INSERT INTO levels VALUES (default, 'Senior');     -- 4
INSERT INTO levels VALUES (default, 'Registrar');  -- 5
INSERT INTO levels VALUES (default, 'Specialist'); -- 6

INSERT INTO genders VALUES (default, 'Female'); -- 1
INSERT INTO genders VALUES (default, 'Male');   -- 2
INSERT INTO genders VALUES (default, 'Other');  -- 3

INSERT INTO confidentialities VALUES (default, 'Official');  -- 1
INSERT INTO confidentialities VALUES (default, 'Sensitive'); -- 2
INSERT INTO confidentialities VALUES (default, 'Protected'); -- 3

INSERT INTO authorities VALUES (default, 'Parent');                    -- 1
INSERT INTO authorities VALUES (default, 'Legal Guardian');            -- 2
INSERT INTO authorities VALUES (default, 'Designated Representative'); -- 3
INSERT INTO authorities VALUES (default, 'Authorised Adult');          -- 4

-- Administrators (Users).

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Alice');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Administrator');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 1);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 4);

-- Secretaries (Users).

-- Bob Buchanan
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Bob Buchanan');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO secretaries (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Bob', 'Buchanan', '1980-05-15', 2, '6085559001');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Secretary');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 2);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 3);

-- Carl Bradford
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Carl Bradford');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO secretaries (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Carl', 'Bradford', '1985-08-22', 2, '6085559002');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Secretary');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 2);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 1);
SET @recovered_user_id = (SELECT entity_id FROM users WHERE username = 'Bob Buchanan');
INSERT INTO user_manager (user_id, manager_id) VALUES (@new_entity_id, @recovered_user_id);

-- Doctors (Users).

-- James Carter
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'James Carter');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO doctors (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'James', 'Carter', '1975-11-03', 2, '6085559003');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 4);

-- Linda Douglas
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Linda Douglas');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO doctors (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Linda', 'Douglas', '1978-02-14', 1, '6085559004');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO doctor_specialties (doctor_id, specialty_id) VALUES (@new_entity_id, 2);
INSERT INTO doctor_specialties (doctor_id, specialty_id) VALUES (@new_entity_id, 3);
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 6);

-- Sharon Jenkins
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Sharon Jenkins');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO doctors (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Sharon', 'Jenkins', '1982-07-09', 1, '6085559005');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 2);

-- Jestine Teixeira
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Jestine Teixeira');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO doctors (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Jestine', 'Teixeira', '1990-12-01', 1, '6085559006');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO user_manager (user_id, manager_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Kathaleen Tramonte
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Kathaleen Tramonte');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO doctors (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Kathaleen', 'Tramonte', '1986-04-18', 1, '6085559007');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 5);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO user_manager (user_id, manager_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Adults.

-- George Franklin (Parent)
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'George', 'Franklin', '1970-03-12', 2, '6085551023');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

-- Betty Davis (Parent)
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Betty', 'Davis', '1975-06-25', 1, '6085551749');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);

-- Eduardo Rodriquez (Parent)
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Eduardo', 'Rodriquez', '1978-09-08', 2, '6085558763');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

-- Jean Coleman (Parent)
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Jean', 'Coleman', '1980-01-30', 1, '6085552654');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);

-- Carlos Estaban (Parent)
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Carlos', 'Estaban', '1976-11-14', 2, '6085555487');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

-- Clara Higgins (School Teacher)
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, birth_date, gender_id, telephone) VALUES (@new_entity_id, 'Clara', 'Higgins', '1982-04-10', 1, '6085559999');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);

-- Patients.

-- Leo Franklin
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Leo', 'Franklin', '2010-09-07', 2, '110 W. Liberty St.', 'Madison');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551023');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Basil Davis
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Basil', 'Davis', '2012-08-06', 2, '638 Cardinal Ave.', 'Sun Prairie');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551749');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Rosy Rodriquez
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Rosy', 'Rodriquez', '2011-04-17', 1, '2693 Commerce St.', 'McFarland');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085558763');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Jewel Rodriquez
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Jewel', 'Rodriquez', '2010-03-07', 1, '2693 Commerce St.', 'McFarland');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085558763');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Samantha Coleman
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Samantha', 'Coleman', '2012-09-04', 1, '105 N. Lake St.', 'Monona');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Kathaleen' AND last_name = 'Tramonte');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Max Coleman
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Max', 'Coleman', '2012-09-04', 2, '105 N. Lake St.', 'Monona');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Kathaleen' AND last_name = 'Tramonte');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Lucky Estaban
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Lucky', 'Estaban', '2011-08-06', 1, '2335 Independence La.', 'Waunakee');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Sly Estaban
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Sly', 'Estaban', '2012-06-08', 2, '2335 Independence La.', 'Waunakee');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visits.

-- Visit 1: Samantha Coleman
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Samantha' AND last_name = 'Coleman' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-01', 1, 'Routine pediatric wellness checkup.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 2: Max Coleman
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Max' AND last_name = 'Coleman' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-02', 1, 'Routine pediatric wellness checkup.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 3: Max Coleman
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Max' AND last_name = 'Coleman' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-03', 2, 'Minor dental surgery and cavity extraction.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Kathaleen' AND last_name = 'Tramonte');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 4: Samantha Coleman
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Samantha' AND last_name = 'Coleman' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-04', 2, 'Tonsillectomy procedure.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Kathaleen' AND last_name = 'Tramonte');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 5: Rosy Rodriquez
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Rosy' AND last_name = 'Rodriquez' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-04', 1, 'Evaluation for persistent fever and cough.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085558763');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 6: Jewel Rodriquez
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Jewel' AND last_name = 'Rodriquez' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-04', 1, 'Treatment for acute otitis media (ear infection).');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085558763');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 7: Lucky Estaban
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Lucky' AND last_name = 'Estaban' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-05', 1, 'School sports physical examination.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 8: Sly Estaban
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Sly' AND last_name = 'Estaban' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-05', 1, 'Standard childhood immunizations schedule update.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 9: Basil Davis
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Basil' AND last_name = 'Davis' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-06', 3, 'Pediatric behavioral health and developmental evaluation.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551749');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 10: Leo Franklin
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Leo' AND last_name = 'Franklin' AND gender_id = 2);
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085559999');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@recovered_patient_id, @recovered_adult_id, 4);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-10', 1, 'School-mandated asthma evaluation after recess incident.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 11: Leo Franklin
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Leo' AND last_name = 'Franklin' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-15', 1, 'Asthma follow-up and inhaler prescription.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551023');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 12: Basil Davis
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Basil' AND last_name = 'Davis' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-12', 1, 'Strep throat test and antibiotic prescription.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551749');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 13: Rosy Rodriquez
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Rosy' AND last_name = 'Rodriquez' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-14', 1, 'Allergy testing for seasonal pollen.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085558763');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 14: Jewel Rodriquez
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Jewel' AND last_name = 'Rodriquez' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-14', 1, 'Treatment for minor abrasion from playground fall.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085558763');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 15: Samantha Coleman
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Samantha' AND last_name = 'Coleman' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-18', 2, 'Post-tonsillectomy recovery follow-up.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 16: Max Coleman
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Max' AND last_name = 'Coleman' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-18', 2, 'Post-dental surgery healing checkup.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 17: Lucky Estaban
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Lucky' AND last_name = 'Estaban' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-20', 1, 'X-ray and splinting for suspected sprained wrist.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 18: Sly Estaban
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Sly' AND last_name = 'Estaban' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-22', 1, 'Consultation for persistent stomach ache.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 19: Leo Franklin (Accompanied by Teacher Clara Higgins)
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Leo' AND last_name = 'Franklin' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-25', 1, 'Vision screening due to reading difficulties in class.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085559999');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visit 20: Basil Davis
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Basil' AND last_name = 'Davis' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-28', 3, 'Follow-up developmental evaluation.');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551749');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
