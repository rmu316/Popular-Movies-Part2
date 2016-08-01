package com.example.richardmu.flixbrite.app;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.example.richardmu.flixbrite.app.data.MovieContract;

import java.util.ArrayList;

/**
 * Created by Richard Mu on 6/6/2016.
 * Class to query the entire database and
 * fetch all favorite movie details
 */
public class QueryFavoritesTask extends AsyncTask<Void, Void, ArrayList<MovieData>> {
    // Globals
    private final String LOG_TAG = QueryFavoritesTask.class.getSimpleName();

    // Members
    private PosterFragment mParent;
    private Context mContext;
    private ArrayList<String> imageUrls;

    public QueryFavoritesTask(PosterFragment parent, Context context) {
        this.mParent = parent;
        this.mContext = context;
    }

    protected ArrayList<MovieData> doInBackground(Void ...params) {
        // Sort order:  Ascending, by title.
        String sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_TITLE + " ASC";
        Cursor cur = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, sortOrder);
        ArrayList<MovieData> allMovieData = new ArrayList<>(cur.getCount());
        imageUrls = new ArrayList<>(cur.getCount());
        if (cur.moveToFirst()) {
            do {
                String mId = cur.getString(cur.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                String mImg = cur.getString(cur.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_IMAGE));
                String mTitle = cur.getString(cur.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE));
                String mSynop = cur.getString(cur.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_SYNOP));
                String mRating = cur.getString(cur.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RATING));
                String mRel = cur.getString(cur.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE));
                MovieData newMovie = new MovieData(mId, mImg, mTitle, mSynop, mRating, mRel);
                allMovieData.add(newMovie);
                imageUrls.add(mImg);
            } while (cur.moveToNext());
        }
        if (cur != null && !cur.isClosed()) {
            cur.close();
        }
        return allMovieData;
    }

    protected void onPostExecute(ArrayList<MovieData> allMovieData) {
        mParent.clearAndSetAdapter(imageUrls.toArray(new String[imageUrls.size()]));
        mParent.setAllMovieData(allMovieData);
    }
}
