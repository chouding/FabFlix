package edu.uci.ics.fabflixmobile.data.model;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {

    private final String id;
    private final String title;
    private final short year;
    private final String director;
    private final String genres;
    private final String stars;
    private final String ratings;

    public Movie(String id, String title, short year, String director, String genres, String stars, String ratings) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
        this.ratings = ratings;

    }
    public String getMovieId(){return id;}
    public String getTitle() {
        return title;
    }

    public short getYear() {
        return year;
    }
    public String getDirector(){return director;}

    public String getGenres(){return genres;}

    public String getStars(){return stars;}

    public String getRatings(){return ratings;}

}