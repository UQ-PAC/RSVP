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
SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Bob Buchanan');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO secretaries (entity_id, first_name, last_name) VALUES (@new_entity_id, 'Bob', 'Buchanan');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Secretary');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 2);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 3);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Carl Bradford');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO secretaries (entity_id, first_name, last_name) VALUES (@new_entity_id, 'Carl', 'Bradford');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Secretary');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 2);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 1);
SET @recovered_user_id = (SELECT entity_id FROM users WHERE username = 'Bob Buchanan');
INSERT INTO user_manager (user_id, manager_id) VALUES (@new_entity_id, @recovered_user_id);

-- Doctors (Users).

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'James Carter');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO doctors (entity_id, first_name, last_name) VALUES (@new_entity_id, 'James', 'Carter');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 4);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Linda Douglas');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO doctors (entity_id, first_name, last_name) VALUES (@new_entity_id, 'Linda', 'Douglas');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO doctor_specialties (doctor_id, specialty_id) VALUES (@new_entity_id, 2);
INSERT INTO doctor_specialties (doctor_id, specialty_id) VALUES (@new_entity_id, 3);
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 6);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Sharon Jenkins');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO doctors (entity_id, first_name, last_name) VALUES (@new_entity_id, 'Sharon', 'Jenkins');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 2);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Jestine Teixeira');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO doctors (entity_id, first_name, last_name) VALUES (@new_entity_id, 'Jestine', 'Teixeira');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO user_manager (user_id, manager_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Kathaleen Tramonte');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO doctors (entity_id, first_name, last_name) VALUES (@new_entity_id, 'Kathaleen', 'Tramonte');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Doctor');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 3);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 5);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO user_manager (user_id, manager_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Adults.

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, telephone) VALUES (@new_entity_id, 'George', 'Franklin', '6085551023');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, telephone) VALUES (@new_entity_id, 'Betty', 'Davis', '6085551749');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, telephone) VALUES (@new_entity_id, 'Eduardo', 'Rodriquez', '6085558763');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, telephone) VALUES (@new_entity_id, 'Jean', 'Coleman', '6085552654');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO adults (entity_id, first_name, last_name, telephone) VALUES (@new_entity_id, 'Carlos', 'Estaban', '6085555487');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Adult');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

-- Patients.

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Leo', 'Franklin', '2010-09-07', 2, '110 W. Liberty St.', 'Madison');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551023');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Basil', 'Davis', '2012-08-06', 2, '638 Cardinal Ave.', 'Sun Prairie');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551749');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

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

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Lucky', 'Estaban', '2011-08-06', 1, '2335 Independence La.', 'Waunakee');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO patients (entity_id, first_name, last_name, birth_date, gender_id, address, city) VALUES (@new_entity_id, 'Sly', 'Estaban', '2012-06-08', 2, '2335 Independence La.', 'Waunakee');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Patient');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO patient_adults (patient_id, adult_id, authority_id) VALUES (@new_entity_id, @recovered_adult_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO patient_doctors (patient_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visits.

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Samantha' AND last_name = 'Coleman' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-01', 1, 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Max' AND last_name = 'Coleman' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-02', 1, 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085552654');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Max' AND last_name = 'Coleman' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-03', 2, 'neutered');
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

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Samantha' AND last_name = 'Coleman' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-04', 2, 'spayed');
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

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Rosy' AND last_name = 'Rodriquez' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-04', 1, 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085558763');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Jewel' AND last_name = 'Rodriquez' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-04', 1, 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085558763');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Lucky' AND last_name = 'Estaban' AND gender_id = 1);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-05', 1, 'vaccination');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Sly' AND last_name = 'Estaban' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-05', 1, 'vaccination');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085555487');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_patient_id = (SELECT entity_id FROM patients WHERE first_name = 'Basil' AND last_name = 'Davis' AND gender_id = 2);
INSERT INTO visits (entity_id, patient_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_patient_id, '2013-01-06', 3, '???');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
SET @recovered_adult_id = (SELECT entity_id FROM adults WHERE telephone = '6085551749');
INSERT INTO visit_adults (visit_id, adult_id) VALUES (@new_entity_id, @recovered_adult_id);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
