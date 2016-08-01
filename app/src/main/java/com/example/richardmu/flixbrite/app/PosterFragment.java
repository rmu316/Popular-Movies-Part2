package com.example.richardmu.flixbrite.app;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The placeholder fragment for displaying all
 * the movies pulled from the server (or the local
 * list of user favorites)
 */
public class PosterFragment extends Fragment {

    // Globals
    private final String LOG_TAG = PosterFragment.class.getSimpleName();
    private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private final String POSTERS = "poster_paths";

    // adapter to hold all movie images
    private CustomImageArrayAdapter mGridAdapter;
    // json returned when fetching movies from
    // the server
    private String mMovieJsonStr;
    // list of all movie information structs which store info for each
    // movie to be displayed
    private ArrayList<MovieData> mAllMovieData = new ArrayList<>();
    // list of all poster paths (images) to be displayed on the main page
    private ArrayList<String> mPosterPaths = new ArrayList<>();
    private boolean mSortByFavorites;
    private TextView mNoImages;
    // determine if we already have a saved instance (i.e. if we just rotated
    // the screen) result from change of app configuration
    private boolean mHasSavedInstance;

    public PosterFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putStringArrayList(POSTERS, mPosterPaths);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new restoreSavedInstance().execute(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mNoImages = (TextView)rootView.findViewById(R.id.no_favs_to_display);
        GridView grid = (GridView)rootView.findViewById(R.id.gridview_poster);
        mGridAdapter = new CustomImageArrayAdapter(getActivity(),
                R.layout.grid_item_poster,
                mPosterPaths);
        grid.setAdapter(mGridAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieData theMovie = mAllMovieData.get(position);
                ((Callback) getActivity()).onItemSelected(theMovie);
            }
        });
        return rootView;
    }

    private void updateMovies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String type = prefs.getString(getString(R.string.pref_sorted_order_key), getString(R.string.pref_sorted_order_default));
        // determine how to update the movies, either by user favorites (from a local db)
        // or by what is fetched from the server
        if (!type.equals(getString(R.string.pref_sorted_order_favorites_val))) {
            mSortByFavorites = false;
            // fetch movies from the server
            new FetchMoviesTask(this).execute(type);
        } else {
            mSortByFavorites = true;
            // retrieve movies from the local database
            new QueryFavoritesTask(this, getActivity()).execute();
        }
    }

    // Setter and getter methods to modify and retrieve
    // member variables from this class
    public String getMovieJsonStr() {
        return mMovieJsonStr;
    }

    public void setMovieJsonStr(String movieJsonStr) {
        mMovieJsonStr = movieJsonStr;
    }

    public void setAllMovieData(ArrayList<MovieData> allMovieData) {
        this.mAllMovieData.clear();
        this.mAllMovieData.addAll(allMovieData);
        displayFirstMovie();
    }

    public String getBaseImageUrl() {
        return IMAGE_BASE_URL;
    }

    public void clearAndSetAdapter(String []fetchedData) {
        mPosterPaths.clear();
        mGridAdapter.clear();
        if (!mSortByFavorites) {
            convertJsonToCustomStruct();
        }
        mPosterPaths.addAll(Arrays.asList(fetchedData));
        mGridAdapter.setMovieData(new ArrayList<>(Arrays.asList(fetchedData)));
        // We only display the first movie in the list
        // by default if the app is just loaded and NOT if
        // its configuration changed (i.e. due to screen rotation)
        if (!mHasSavedInstance) {
            displayFirstMovie();
        }
        if (mGridAdapter.isEmpty() && mSortByFavorites) {
            mNoImages.setText(getString(R.string.no_movie_favs));
            mNoImages.setVisibility(View.VISIBLE);
        } else {
            mNoImages.setVisibility(View.GONE);
        }
    }

    // For two-pane layout on tablets, when
    // the screen is initially displayed,
    // we by default display the first movie
    private void displayFirstMovie() {
        if (!mAllMovieData.isEmpty()) {
            ((Callback) getActivity()).displayFirstItem(mAllMovieData.get(0));
        }
    }

    // important helper function to convert a JSON
    // into a MovieData struct
    private void convertJsonToCustomStruct() {
        final String TMDB_RESULT = "results";
        final String TMDB_POSTER = "poster_path";
        final String TMDB_TITLE = "original_title";
        final String TMDB_SYNOP = "overview";
        final String TMDB_RATING = "vote_average";
        final String TMDB_REL = "release_date";
        final String TMDB_ID = "id";

        mAllMovieData.clear();

        try {
            JSONObject movieJsonObj = new JSONObject(mMovieJsonStr);
            JSONArray movieArray = movieJsonObj.getJSONArray(TMDB_RESULT);
            Integer MOVIE_SIZE = movieArray.length();

            for (int i = 0; i < MOVIE_SIZE; i++) {
                JSONObject oneResult = movieArray.getJSONObject(i);
                String mId = oneResult.getString(TMDB_ID);
                String mImg = oneResult.getString(TMDB_POSTER);
                String mTitle = oneResult.getString(TMDB_TITLE);
                String mSynop = oneResult.getString(TMDB_SYNOP);
                String mRating = oneResult.getString(TMDB_RATING);
                String mRel = oneResult.getString(TMDB_REL);
                MovieData newMovie = new MovieData(mId, mImg, mTitle, mSynop, mRating, mRel);
                mAllMovieData.add(newMovie);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(MovieData theMovie);

        /**
         * DetailFragmentCallback for displaying the first movie
         * in the list on two-pane layout
         */
        public void displayFirstItem(MovieData theMovie);
    }

    // use another thread to restore saved instances so
    // as to not overburden the main thread
    class restoreSavedInstance extends AsyncTask<Bundle, Void, Void> {

        protected Void doInBackground(Bundle ...params) {
            if (params[0] != null) {
                mHasSavedInstance = true;
                mPosterPaths.clear();
                // retrieve the saved list of movies from
                // the previous instance before the config
                mPosterPaths.addAll(params[0].getStringArrayList(POSTERS));
            } else {
                mHasSavedInstance = false;
            }
            return null;
        }
    }
}
