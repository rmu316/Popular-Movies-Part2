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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Richard Mu on 6/1/2016.
 * Async method to fetch all trailers info for a particular movie
 */
public class FetchTrailersTask extends AsyncTask<String, Void, String> {
    // Globals
    private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();
    private String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private final String APP_ID = "3f86fa8fe74139faf77b6349c144fcdc";
    private final String API_KEY = "api_key";
    private final String TRAILER_PATH = "videos";

    // Members
    private DetailActivityFragment mParent;

    public FetchTrailersTask(DetailActivityFragment parent) {
        this.mParent = parent;
    }

    protected String doInBackground(String ...params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            String NEW_BASE_URL = BASE_URL.concat(params[0]);
            Uri trailerUrl = Uri.parse(NEW_BASE_URL).buildUpon()
                    .appendPath(TRAILER_PATH)
                    .appendQueryParameter(API_KEY, APP_ID)
                    .build();
            URL url = new URL(trailerUrl.toString());
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
            return buffer.toString();
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
    }

    protected void onPostExecute(String trailersJson) {
        final String TMDB_RESULT = "results";
        final String TMDB_KEY = "key";
        final String TMDB_NAME = "name";
        try {
            JSONObject trailerJsonObj = new JSONObject(trailersJson);
            JSONArray trailerArray = trailerJsonObj.getJSONArray(TMDB_RESULT);
            Integer TRAILERS_SIZE = trailerArray.length();
            Map<String, String> trailerInfo = new HashMap<>();

            for (int i = 0; i < TRAILERS_SIZE; i++) {
                JSONObject oneResult = trailerArray.getJSONObject(i);
                String key = oneResult.getString(TMDB_KEY);
                String name = oneResult.getString(TMDB_NAME);
                trailerInfo.put(name, key);
            }
            mParent.setMovieInfo(trailerInfo, true);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
