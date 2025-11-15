CREATE TABLE IF NOT EXISTS mpa_ratings (
    mpa_id INTEGER PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id INTEGER PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50),
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    film_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    mpa_id INTEGER REFERENCES mpa_ratings(mpa_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id INTEGER REFERENCES films(film_id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres(genre_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS likes (
    film_id INTEGER REFERENCES films(film_id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS friends (
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    friend_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);
CREATE TABLE IF NOT EXISTS directors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_directors (
    film_id BIGINT NOT NULL,
    director_id INT NOT NULL,
    PRIMARY KEY (film_id, director_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (director_id) REFERENCES directors(id) ON DELETE CASCADE
);