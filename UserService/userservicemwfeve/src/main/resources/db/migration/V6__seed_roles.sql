INSERT INTO `role` (name, deleted) SELECT 'USER', false FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE name = 'USER');
INSERT INTO `role` (name, deleted) SELECT 'ADMIN', false FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE name = 'ADMIN');
INSERT INTO `role` (name, deleted) SELECT 'MENTOR', false FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE name = 'MENTOR');
INSERT INTO `role` (name, deleted) SELECT 'STUDENT', false FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE name = 'STUDENT');
