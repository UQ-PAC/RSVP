INSERT INTO databases VALUES (default, 'Owners (A)');        -- 1
INSERT INTO databases VALUES (default, 'Pets (A)');          -- 2
INSERT INTO databases VALUES (default, 'Veterinarians (A)'); -- 3
INSERT INTO databases VALUES (default, 'Visits (A)');        -- 4
INSERT INTO databases VALUES (default, 'Users (A)');         -- 5
INSERT INTO databases VALUES (default, 'Owners (B)');        -- 6
INSERT INTO databases VALUES (default, 'Pets (B)');          -- 7
INSERT INTO databases VALUES (default, 'Veterinarians (B)'); -- 8
INSERT INTO databases VALUES (default, 'Visits (B)');        -- 9
INSERT INTO databases VALUES (default, 'Users (B)');         -- 10

INSERT INTO specialties VALUES (default, 'radiology'); -- 1
INSERT INTO specialties VALUES (default, 'surgery');   -- 2
INSERT INTO specialties VALUES (default, 'dentistry'); -- 3

-- Users and Veterinarians.

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'James Carter');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 5);
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 10);
INSERT INTO vets (entity_id, first_name, last_name) VALUES (@new_entity_id, 'James', 'Carter');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Veterinarian');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 3);
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 8);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Linda Douglas');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 5);
INSERT INTO vets (entity_id, first_name, last_name) VALUES (@new_entity_id, 'Linda', 'Douglas');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Veterinarian');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 3);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (@new_entity_id, 2);
INSERT INTO vet_specialties (vet_id, specialty_id) VALUES (@new_entity_id, 3);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO users (entity_id, username) VALUES (@new_entity_id, 'Sharon Jenkins');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'User');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 5);
INSERT INTO vets (entity_id, first_name, last_name) VALUES (@new_entity_id, 'Sharon', 'Jenkins');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Veterinarian');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 3);

-- Owners.

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO owners (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Owner');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 1);
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 6);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO owners (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Owner');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 6);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO owners (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Owner');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 1);

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
INSERT INTO owners (entity_id, first_name, last_name, address, city, telephone) VALUES (@new_entity_id, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Owner');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 6);

-- Pets.

INSERT INTO types VALUES (default, 'cat');     -- 1
INSERT INTO types VALUES (default, 'dog');     -- 2
INSERT INTO types VALUES (default, 'lizard');  -- 3
INSERT INTO types VALUES (default, 'snake');   -- 4
INSERT INTO types VALUES (default, 'bird');    -- 5
INSERT INTO types VALUES (default, 'hamster'); -- 6

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085551023');
INSERT INTO pets (entity_id, name, birth_date, type_id, owner_id) VALUES (@new_entity_id, 'Leo', '2010-09-07', 1, @recovered_owner_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Pet');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 2);
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 7);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Owner: George Franklin
-- Vets: James Carter

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085558763');
INSERT INTO pets (entity_id, name, birth_date, type_id, owner_id) VALUES (@new_entity_id, 'Rosy', '2011-04-17', 2, @recovered_owner_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Pet');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 7);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Owner: Eduardo Rodriquez
-- Vets: James Carter

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085558763');
INSERT INTO pets (entity_id, name, birth_date, type_id, owner_id) VALUES (@new_entity_id, 'Jewel', '2010-03-07', 2, @recovered_owner_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Pet');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 7);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Owner: Eduardo Rodriquez
-- Vets: James Carter

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085552654');
INSERT INTO pets (entity_id, name, birth_date, type_id, owner_id) VALUES (@new_entity_id, 'Samantha', '2012-09-04', 1, @recovered_owner_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Pet');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 2);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Owner: Jean Coleman
-- Vets: Sharon Jenkins, Linda Douglas

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085552654');
INSERT INTO pets (entity_id, name, birth_date, type_id, owner_id) VALUES (@new_entity_id, 'Max', '2012-09-04', 1, @recovered_owner_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Pet');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 2);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Owner: Jean Coleman
-- Vets: Sharon Jenkins, Linda Douglas

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085555487');
INSERT INTO pets (entity_id, name, birth_date, type_id, owner_id) VALUES (@new_entity_id, 'Lucky', '2011-08-06', 5, @recovered_owner_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Pet');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 7);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Owner: Carlos Estaban
-- Vets: James Carter

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085555487');
INSERT INTO pets (entity_id, name, birth_date, type_id, owner_id) VALUES (@new_entity_id, 'Sly', '2012-06-08', 1, @recovered_owner_id);
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Pet');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 7);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO pet_vets (pet_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Owner: Carlos Estaban
-- Vets: James Carter

-- Visits.

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085552654');
SET @recovered_pet_id = (SELECT entity_id FROM pets WHERE name = 'Samantha' AND type_id = 1 AND owner_id = @recovered_owner_id);
INSERT INTO visits (entity_id, pet_id, visit_date, description) VALUES (@new_entity_id, @recovered_pet_id, '2013-01-01', 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 4);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Pet: Samantha

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085552654');
SET @recovered_pet_id = (SELECT entity_id FROM pets WHERE name = 'Max' AND type_id = 1 AND owner_id = @recovered_owner_id);
INSERT INTO visits (entity_id, pet_id, visit_date, description) VALUES (@new_entity_id, @recovered_pet_id, '2013-01-02', 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 4);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Pet: Max

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085552654');
SET @recovered_pet_id = (SELECT entity_id FROM pets WHERE name = 'Max' AND type_id = 1 AND owner_id = @recovered_owner_id);
INSERT INTO visits (entity_id, pet_id, visit_date, description) VALUES (@new_entity_id, @recovered_pet_id, '2013-01-03', 'neutered');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 4);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Pet: Max

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085552654');
SET @recovered_pet_id = (SELECT entity_id FROM pets WHERE name = 'Samantha' AND type_id = 1 AND owner_id = @recovered_owner_id);
INSERT INTO visits (entity_id, pet_id, visit_date, description) VALUES (@new_entity_id, @recovered_pet_id, '2013-01-04', 'spayed');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 4);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Sharon' AND last_name = 'Jenkins');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'Linda' AND last_name = 'Douglas');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Pet: Samantha

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085558763');
SET @recovered_pet_id = (SELECT entity_id FROM pets WHERE name = 'Rosy' AND type_id = 2 AND owner_id = @recovered_owner_id);
INSERT INTO visits (entity_id, pet_id, visit_date, description) VALUES (@new_entity_id, @recovered_pet_id, '2013-01-04', 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 9);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Pet: Rosy

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085558763');
SET @recovered_pet_id = (SELECT entity_id FROM pets WHERE name = 'Jewel' AND type_id = 2 AND owner_id = @recovered_owner_id);
INSERT INTO visits (entity_id, pet_id, visit_date, description) VALUES (@new_entity_id, @recovered_pet_id, '2013-01-04', 'rabies shot');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 9);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Pet: Jewel

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085555487');
SET @recovered_pet_id = (SELECT entity_id FROM pets WHERE name = 'Lucky' AND type_id = 5 AND owner_id = @recovered_owner_id);
INSERT INTO visits (entity_id, pet_id, visit_date, description) VALUES (@new_entity_id, @recovered_pet_id, '2013-01-05', 'vaccination');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 9);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Pet: Lucky

SET @new_entity_id = (SELECT entity_id FROM FINAL TABLE (INSERT INTO entities DEFAULT VALUES));
SET @recovered_owner_id = (SELECT entity_id FROM owners WHERE telephone = '6085555487');
SET @recovered_pet_id = (SELECT entity_id FROM pets WHERE name = 'Sly' AND type_id = 1 AND owner_id = @recovered_owner_id);
INSERT INTO visits (entity_id, pet_id, visit_date, description) VALUES (@new_entity_id, @recovered_pet_id, '2013-01-05', 'vaccination');
INSERT INTO entity_types (entity_id, entity_type) VALUES (@new_entity_id, 'Visit');
INSERT INTO entity_databases (entity_id, database_id) VALUES (@new_entity_id, 9);
SET @recovered_vet_id = (SELECT entity_id FROM vets WHERE first_name = 'James' AND last_name = 'Carter');
INSERT INTO visit_vets (visit_id, vet_id) VALUES (@new_entity_id, @recovered_vet_id);
-- Pet: Sly
