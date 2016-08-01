package com.example.richardmu.flixbrite.app;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.richardmu.flixbrite.app.data.MovieContract;

/**
 * Created by Richard Mu on 6/7/2016.
 * Async class to add or remove movies from
 * the local database (which stores a users'
 * favorite movies)
 */
public class QueryMovieTask extends AsyncTask<String, Void, String> {
    // Globals
    private final String LOG_TAG = QueryMovieTask.class.getSimpleName();

    // Member variables
    private Context mContext;
    private boolean mAddOrDelete;

    public QueryMovieTask(Context context) {
        this.mContext = context;
    }

    protected String doInBackground(String ...params) {
        if (params[0].equals("add")) {
            return addToDatabase(params[1], params[2], params[3],
                    params[4], params[5], params[6]);
        }
        return removeFromDatabase(params[1]);
    }

    private String addToDatabase(String mId, String mImg, String mTitle,
                          String mSynop, String mRating, String mRel) {
        mAddOrDelete = false;
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mId);
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_IMAGE, mImg);
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, mTitle);
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_SYNOP, mSynop);
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATING, mRating);
        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE, mRel);

        return mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, cv).getPath();
    }

    private String removeFromDatabase(String mId) {
        mAddOrDelete = true;
        String whereClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String []selectionArgs = new String[]{mId};

        return Integer.toString(mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, whereClause, selectionArgs));
    }

    protected void onPostExecute(String returnValue) {
        Log.v(LOG_TAG, mAddOrDelete ? "Delete finished and returned " + returnValue + " row(s) deleted" :
                                     "Insert finished and returned this uri: " + returnValue);
    }
}
