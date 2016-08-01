package com.example.richardmu.flixbrite.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Richard Mu on 6/1/2016.
 * Async class to fetch movie info from the server
 */
public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {
    // Globals
    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private final String APP_ID = "3f86fa8fe74139faf77b6349c144fcdc";
    private final String API_KEY = "api_key";

    // Members
    private PosterFragment mParent;

    public FetchMoviesTask(PosterFragment parent) {
        this.mParent = parent;
    }

    protected String[] doInBackground(String ...params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            BASE_URL = BASE_URL.concat(params[0]);
            Uri builder = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY,APP_ID)
                    .build();
            URL url = new URL(builder.toString());
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                return null;
            }
            mParent.setMovieJsonStr(buffer.toString());
            try {
                return getMovieDataFromJson(mParent.getMovieJsonStr());
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }

    protected void onPostExecute(String []fetchedData) {
        if (fetchedData != null) {
            mParent.clearAndSetAdapter(fetchedData);
        }
    }

    // Get list of all image urls supplied by the movie database API
    private String[] getMovieDataFromJson(String movieJsonStr) throws JSONException {
        final String TMDB_RESULT = "results";
        final String TMDB_POSTER = "poster_path";
        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULT);
        Integer MOVIE_SIZE = movieArray.length();

        String []resultStrs = new String[MOVIE_SIZE];

        for (int i = 0; i < MOVIE_SIZE; i++) {
            JSONObject oneResult = movieArray.getJSONObject(i);
            String poster = oneResult.getString(TMDB_POSTER);
            resultStrs[i] = mParent.getBaseImageUrl() + poster;
        }
        return resultStrs;
    }
}
