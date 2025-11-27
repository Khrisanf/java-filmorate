MERGE INTO MPA_RATINGS (id, name) KEY (id) VALUES (1, 'G');

INSERT INTO USERS (id, email, login, name, birthday)
VALUES (1, 'u1@example.com', 'user1', 'User One', DATE '1990-01-01'),
       (2, 'u2@example.com', 'user2', 'User Two', DATE '1990-01-01'),
       (3, 'u3@example.com', 'user3', 'User Three', DATE '1990-01-01'),
       (4, 'u4@example.com', 'user4', 'User Four', DATE '1990-01-01'),
       (5, 'u5@example.com', 'user5', 'User Five', DATE '1990-01-01'),
       (6, 'u6@example.com', 'user6', 'User Six', DATE '1990-01-01'),
       (7, 'u7@example.com', 'user7', 'User Seven', DATE '1990-01-01'),
       (8, 'u8@example.com', 'user8', 'User Eight', DATE '1990-01-01'),
       (9, 'u9@example.com', 'user9', 'User Nine', DATE '1990-01-01');

INSERT INTO FILMS (id, name, description, release_date, duration, mpa_rating_id)
VALUES (1, 'Крадущийся тигр', 'test', DATE '2000-01-01', 120, 1),
       (2, 'Крадущийся дракон', 'test', DATE '2000-01-02', 110, 1),
       (3, 'Крадущийся в ночи', 'test', DATE '2000-01-03', 100, 1);

INSERT INTO likes (film_id, user_id)
VALUES (1, 1),
       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (3, 1),
       (3, 2),
       (3, 3);
