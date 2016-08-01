package com.example.richardmu.flixbrite.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Richard Mu on 6/4/2016.
 * Contract which stores all database/table names and entries
 * used by the internal database which stores all user favorite
 * movies
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.richardmu.flixbrite.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAVORITE = "favorites";

    //Inner class that defines the table contents of the movie favorites table
    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";

        // id of the movie
        public static final String COLUMN_MOVIE_ID = "id";
        // url of the image thumbnail
        public static final String COLUMN_MOVIE_IMAGE = "image";
        // name of movie
        public static final String COLUMN_MOVIE_TITLE = "title";
        // synopsis of movie
        public static final String COLUMN_MOVIE_SYNOP = "synopsis";
        // movie's rating
        public static final String COLUMN_MOVIE_RATING = "rating";
        // release date of movie
        public static final String COLUMN_MOVIE_RELEASE = "release";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
