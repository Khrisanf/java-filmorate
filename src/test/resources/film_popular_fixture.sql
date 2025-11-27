INSERT INTO users (id, email, login, name, birthday)
VALUES (1, 'u1@mail.ru', 'user1', 'User 1', '1990-01-01'),
       (2, 'u2@mail.ru', 'user2', 'User 2', '1990-01-01'),
       (3, 'u3@mail.ru', 'user3', 'User 3', '1990-01-01'),
       (4, 'u4@mail.ru', 'user4', 'User 4', '1990-01-01'),
       (5, 'u5@mail.ru', 'user5', 'User 5', '1990-01-01'),
       (6, 'u6@mail.ru', 'user6', 'User 6', '1990-01-01');

INSERT INTO mpa_ratings (id, name)
VALUES (1, 'G');

INSERT INTO genres (id, name)
VALUES (1, 'Комедия');
INSERT INTO genres (id, name)
VALUES (2, 'Драма');

INSERT INTO films (id, name, description, release_date, duration, mpa_rating_id)
VALUES (1, 'Film1', 'f1', '2000-01-01', 100, 1),
       (2, 'Film2', 'f2', '2000-01-01', 90, 1),
       (3, 'Film3', 'f3', '2001-01-01', 110, 1);

INSERT INTO film_genres (film_id, genre_id)
VALUES (1, 1),
       (2, 2),
       (3, 1);

INSERT INTO likes (film_id, user_id)
VALUES (1, 1),
       (1, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (3, 6);
