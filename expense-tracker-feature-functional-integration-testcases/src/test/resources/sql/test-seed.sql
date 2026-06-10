INSERT INTO expense_types (id, name) VALUES (101, 'Food');
INSERT INTO expense_types (id, name) VALUES (102, 'Transport');
INSERT INTO expense_types (id, name) VALUES (103, 'Utilities');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (1001, 'Seed Lunch', 'Baseline seeded food expense', 20.00, 101, DATE '2026-06-01', TIMESTAMP '2026-06-01 10:00:00', TIMESTAMP '2026-06-01 10:00:00');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (1002, 'Seed Taxi', 'Baseline seeded transport expense', 35.50, 102, DATE '2026-06-02', TIMESTAMP '2026-06-02 11:00:00', TIMESTAMP '2026-06-02 11:00:00');

INSERT INTO expenses (id, title, description, amount, expense_type_id, expense_date, created_at, updated_at)
VALUES (1003, 'Seed Power Bill', 'Baseline seeded utilities expense', 75.25, 103, DATE '2026-06-03', TIMESTAMP '2026-06-03 09:30:00', TIMESTAMP '2026-06-03 09:30:00');
