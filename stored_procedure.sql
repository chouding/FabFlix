use moviedb;

DELIMITER $$
CREATE PROCEDURE add_star (IN input_name VARCHAR(100), IN input_year INT, OUT msg VARCHAR(2000))
BEGIN
	DECLARE is_in INTEGER;
	DECLARE new_id varchar(10);
    DECLARE count INTEGER;


    SET is_in = (SELECT COUNT(*) FROM stars WHERE stars.name = input_name AND stars.birthYear = input_year);
    IF is_in > 0 THEN
		SET msg = (SELECT "Fail: Duplicate Entries");
ELSE
		SET count = 1;
		REPEAT
SET new_id = (SELECT concat(substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
						  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1)));
			SET count = (SELECT COUNT(*) FROM stars WHERE stars.id = @new_id);

		UNTIL count = 0 END REPEAT;
INSERT INTO stars values (new_id, input_name, input_year);
SET msg = (SELECT CONCAT("Success: new star id: ", new_id));
END IF;
END
$$
-- Change back DELIMITER to ;
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE add_movie (IN input_title varchar(100), IN input_year INTEGER, IN input_director varchar(100), IN input_star varchar(100),
							IN input_genre varchar(32), OUT msg VARCHAR(2000))
BEGIN
    DECLARE movie_is_exists INTEGER;
    DECLARE star_id char(10);
    DECLARE genre_id INTEGER;
	DECLARE movie_id varchar(10);
	DECLARE count INTEGER;

    -- add new movie into entry
    SET movie_is_exists = (SELECT COUNT(*) FROM movies WHERE title=input_title AND director=input_director AND year=input_year);
	IF movie_is_exists = 1 THEN
		SET msg = (SELECT "Fail: Duplicate Entry");
ELSE
		SET count = 1;
		REPEAT
SET movie_id = (SELECT concat(substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1),
								  substring('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789', rand()*36+1, 1)));
			SET count = (SELECT COUNT(*) FROM movies WHERE movies.id = movie_id);
		UNTIL count = 0 END REPEAT;
INSERT INTO movies VALUES (movie_id, input_title, input_year, input_director);
INSERT INTO ratings VALUES (movie_id, 0.0, 0);

-- add star to star_in_movies (if not add mew entry to stars table)
SET star_id = (SELECT id from stars where stars.name = input_star limit 1);
		IF star_id is NULL THEN
			CALL add_star(input_star, Null, @n);
			SET star_id = (SELECT id from stars where stars.name = input_star limit 1);
END IF;
INSERT INTO stars_in_movies VALUES (star_id, movie_id);

-- add genre into genres_in_movies (if not add new entry to genres table)
SET genre_id = (SELECT id FROM genres WHERE genres.name = input_genre);
    IF genre_id is NULL THEN
		INSERT INTO genres (name) VALUES(input_genre);
        SET genre_id = (SELECT id FROM genres WHERE genres.name = input_genre);
END IF;
INSERT INTO genres_in_movies VALUES (genre_id, movie_id);
SET msg = (SELECT CONCAT("Success: movieId = ", movie_id, " genreID = ",
			genre_id, " starId = ", star_id));
END IF;

END
$$
-- Change back DELIMITER to ;
DELIMITER ;

