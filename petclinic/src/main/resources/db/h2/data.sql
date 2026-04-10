INSERT INTO clinics VALUES (default, 'Clinic A'); -- 1
INSERT INTO clinics VALUES (default, 'Clinic B'); -- 2

INSERT INTO specialties VALUES (default, 'Radiology'); -- 1
INSERT INTO specialties VALUES (default, 'Surgery');   -- 2
INSERT INTO specialties VALUES (default, 'Dentistry'); -- 3

INSERT INTO roles VALUES (default, 'Administrator'); -- 1
INSERT INTO roles VALUES (default, 'Secretary');     -- 2
INSERT INTO roles VALUES (default, 'Doctor');  -- 3

INSERT INTO levels VALUES (default, 'Intern');      -- 1
INSERT INTO levels VALUES (default, 'Resident');    -- 2
INSERT INTO levels VALUES (default, 'Staff');       -- 3
INSERT INTO levels VALUES (default, 'Senior');      -- 4
INSERT INTO levels VALUES (default, 'Registrar');   -- 5
INSERT INTO levels VALUES (default, 'Specialist');  -- 6

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
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Bob');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Secretary');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 2);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 3);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Carl');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Secretary');
INSERT INTO user_roles (user_id, role_id) VALUES (@new_entity_id, 2);
INSERT INTO user_levels (user_id, level_id) VALUES (@new_entity_id, 1);
SET @recovered_user_id = (SELECT entity_id FROM users WHERE username = 'Bob');
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

-- Parents.

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO parents (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Parent');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO parents (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Parent');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO parents (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Parent');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO parents (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Parent');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO parents (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Parent');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);

-- Children.

INSERT INTO types VALUES (default, 'cat');     -- 1
INSERT INTO types VALUES (default, 'dog');     -- 2
INSERT INTO types VALUES (default, 'lizard');  -- 3
INSERT INTO types VALUES (default, 'snake');   -- 4
INSERT INTO types VALUES (default, 'bird');    -- 5
INSERT INTO types VALUES (default, 'hamster'); -- 6

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085551023');
INSERT INTO children (entity_id, name, birth_date, type_id, parent_id) VALUES (@new_entity_id, 'Leo', '2010-09-07', 1, @recovered_parent_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Child');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085551749');
INSERT INTO children (entity_id, name, birth_date, type_id, parent_id) VALUES (@new_entity_id, 'Basil', '2012-08-06', 6, @recovered_parent_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Child');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085558763');
INSERT INTO children (entity_id, name, birth_date, type_id, parent_id) VALUES (@new_entity_id, 'Rosy', '2011-04-17', 2, @recovered_parent_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Child');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085558763');
INSERT INTO children (entity_id, name, birth_date, type_id, parent_id) VALUES (@new_entity_id, 'Jewel', '2010-03-07', 2, @recovered_parent_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Child');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085552654');
INSERT INTO children (entity_id, name, birth_date, type_id, parent_id) VALUES (@new_entity_id, 'Samantha', '2012-09-04', 1, @recovered_parent_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Child');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Kathaleen' AND last_name = 'Tramonte');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085552654');
INSERT INTO children (entity_id, name, birth_date, type_id, parent_id) VALUES (@new_entity_id, 'Max', '2012-09-04', 1, @recovered_parent_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Child');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Kathaleen' AND last_name = 'Tramonte');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085555487');
INSERT INTO children (entity_id, name, birth_date, type_id, parent_id) VALUES (@new_entity_id, 'Lucky', '2011-08-06', 5, @recovered_parent_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Child');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085555487');
INSERT INTO children (entity_id, name, birth_date, type_id, parent_id) VALUES (@new_entity_id, 'Sly', '2012-06-08', 1, @recovered_parent_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Child');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO child_doctors (child_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

-- Visits.

INSERT INTO confidentialities VALUES (default, 'Official');  -- 1
INSERT INTO confidentialities VALUES (default, 'Sensitive'); -- 2
INSERT INTO confidentialities VALUES (default, 'Protected'); -- 3

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085552654');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Samantha' AND type_id = 1 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-01', 1, 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085552654');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Max' AND type_id = 1 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-02', 1, 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085552654');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Max' AND type_id = 1 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-03', 2, 'neutered');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Kathaleen' AND last_name = 'Tramonte');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085552654');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Samantha' AND type_id = 1 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-04', 2, 'spayed');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Kathaleen' AND last_name = 'Tramonte');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085558763');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Rosy' AND type_id = 2 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-04', 1, 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085558763');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Jewel' AND type_id = 2 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-04', 1, 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 2);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'Jestine' AND last_name = 'Teixeira');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085555487');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Lucky' AND type_id = 5 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-05', 1, 'vaccination');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085555487');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Sly' AND type_id = 1 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-05', 1, 'vaccination');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_parent_id = (SELECT entity_id FROM parents WHERE telephone = '6085551749');
SET @recovered_child_id = (SELECT entity_id FROM children WHERE name = 'Basil' AND type_id = 6 AND parent_id = @recovered_parent_id);
INSERT INTO visits (entity_id, child_id, visit_date, confidentiality_id, description) VALUES (@new_entity_id, @recovered_child_id, '2013-01-06', 3, '???');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_clinics (entity_id, clinic_id) VALUES (@new_entity_id, 1);
SET @recovered_doctor_id = (SELECT entity_id FROM doctors WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_doctors (visit_id, doctor_id) VALUES (@new_entity_id, @recovered_doctor_id);
