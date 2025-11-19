-- Тестовые режиссёры
MERGE INTO DIRECTORS (id, name) KEY (id) VALUES (1, 'Test Director 1'),
                                                (2, 'Test Director 2'),
                                                (3, 'Test Director 3');
MERGE INTO FILMS (id, name, description, release_date, duration) KEY (id) VALUES (1, 'Director Test Film 1',
                                                                                  'Film for DirectorDbStorageTest',
                                                                                  DATE '2000-01-01', 100),
                                                                                 (2, 'Director Test Film 2',
                                                                                  'Film for FilmDbStorageDirectorTest',
                                                                                  DATE '2010-01-01', 120);
