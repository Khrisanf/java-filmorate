-- Тестовые режиссёры
INSERT INTO DIRECTORS (name) VALUES ('Test Director 1'),
                                                ('Test Director 2'),
                                                ('Test Director 3');
INSERT INTO FILMS (name, description, release_date, duration) VALUES ('Director Test Film 1',
                                                                                  'Film for DirectorDbStorageTest',
                                                                                  DATE '2000-01-01', 100),
                                                                                 ('Director Test Film 2',
                                                                                  'Film for FilmDbStorageDirectorTest',
                                                                                  DATE '2010-01-01', 120);
