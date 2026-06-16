INSERT INTO expense_types (id, name) VALUES (1, 'Food');
INSERT INTO expense_types (id, name) VALUES (2, 'Transport');
INSERT INTO expense_types (id, name) VALUES (3, 'Utilities');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (1, 'Breakfast', 'Office breakfast', 12.50, 1, DATE '2026-06-01', TIMESTAMP '2026-06-01 08:00:00', TIMESTAMP '2026-06-01 08:00:00');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (2, 'Cab', 'Airport drop', 45.00, 2, DATE '2026-06-02', TIMESTAMP '2026-06-02 09:00:00', TIMESTAMP '2026-06-02 09:00:00');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (3, 'Dinner', 'Client dinner', 80.25, 1, DATE '2026-06-05', TIMESTAMP '2026-06-05 20:00:00', TIMESTAMP '2026-06-05 20:00:00');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (4, 'Electricity', 'Monthly bill', 100.00, 3, DATE '2026-06-10', TIMESTAMP '2026-06-10 10:00:00', TIMESTAMP '2026-06-10 10:00:00');

ALTER TABLE expense_types ALTER COLUMN id RESTART WITH 100;
ALTER TABLE expenses ALTER COLUMN id RESTART WITH 100;

ALTER TABLE expense_types ALTER COLUMN id RESTART WITH 10;
ALTER TABLE expenses ALTER COLUMN id RESTART WITH 10;
