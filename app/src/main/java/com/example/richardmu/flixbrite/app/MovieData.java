package com.example.richardmu.flixbrite.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Richard Mu on 5/27/2016.
 * Custom class to store and transfer a
 * movie's details. Must be parcelable
 * so that it can be stored and passed
 * in between classes
 */
public class MovieData implements Parcelable {

    private String movieId;
    private String imageURL;
    private String movieTitle;
    private String movieSynop;
    private String movieRating;
    private String movieRel;

    public MovieData(String movieId, String imageURL, String movieTitle,
                     String movieSynop, String movieRating, String movieRel) {
        this.movieId = movieId;
        this.imageURL = imageURL;
        this.movieTitle = movieTitle;
        this.movieSynop = movieSynop;
        this.movieRating = movieRating;
        this.movieRel = movieRel;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getMovieSynop() {
        return movieSynop;
    }

    public String getMovieRating() {
        return movieRating;
    }

    public String getMovieRel() {
        return movieRel;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public void setImageURL(String imageUrl) {
        this.imageURL = imageUrl;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public void setMovieSynop(String movieSynop) {
        this.movieSynop = movieSynop;
    }

    public void setMovieRating(String movieRating) {
        this.movieRating = movieRating;
    }

    public void setMovieRel(String movieRel) {
        this.movieRel = movieRel;
    }

    // Parcelling part
    public MovieData(Parcel in){
        String[] data = new String[6];

        in.readStringArray(data);
        this.movieId = data[0];
        this.imageURL = data[1];
        this.movieTitle = data[2];
        this.movieSynop = data[3];
        this.movieRating = data[4];
        this.movieRel = data[5];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.movieId,
                this.imageURL,
                this.movieTitle,
                this.movieSynop,
                this.movieRating,
                this.movieRel});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MovieData createFromParcel(Parcel in) {
            return new MovieData(in);
        }

        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };
}
