package com.example.richardmu.flixbrite.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.richardmu.flixbrite.app.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The placeholder fragment for the Movie Details page.
 */
public class DetailActivityFragment extends Fragment {

    // Globals
    private final String LOG_TAG = PosterFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w342/";

    // Adapters for holding trailer and review information
    private ArrayAdapter<String> mTrailerAdapter, mReviewAdapter;
    // mapping of trailer names to their ids (used by youtube)
    private Map<String, String> mTrailerNameAndKey = new HashMap<>();
    // mapping of review author to urls (used to lookup the review)
    private Map<String, String> mReviewAuthorAndUrl = new HashMap<>();
    // current state of movie as a user favorite
    private boolean mIsMovieFavorite;
    // text which this app sends to another app for sharing
    private String mShareText;
    private ShareActionProvider mShareActionProvider;
    private MovieData mTheMovie;
    // Context
    private Context mContext;

    // All the GUI widgets!
    private FrameLayout mTitleBackground;
    private ImageView mMovieImage;
    private Button mFavButton;
    private ListView mMovieTrailers, mMovieReviews;
    private TextView mMovieTitle, mRatingTitle, mMovieRating,
                     mReleaseTitle, mMovieRelease, mSynopTitle,
                     mMovieSynop, mTrailerTitle, mReviewTitle;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private Intent sharedIntentMaker(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareText);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareText != null) {
            mShareActionProvider.setShareIntent(sharedIntentMaker());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    // Convert a date like 04-29-2016 => Apr 29, 2016
    public String parseDateToReadable(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "MMM dd, yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    // Function for displaying a trailer (in the youtube browser) when it is clicked on
    public void watchYoutubeVideo(String id){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            // Verify that the intent will resolve to an activity
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    // Function for redirecting user to review page when it is clicked on
    public void readMovieReview(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }

    // We need to readjust the view of the trailers/reviews listview
    // so that all elements of the views are displayed.
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the MovieData struct containing all movie details
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTheMovie = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        if (mTheMovie != null) {

            // First, initialize all the GUI widgets from the XML
            mTitleBackground = (FrameLayout) rootView.findViewById(R.id.title_background);
            mMovieTitle = (TextView) rootView.findViewById(R.id.movie_title);
            mMovieImage = (ImageView) rootView.findViewById(R.id.movie_image);
            mRatingTitle = (TextView) rootView.findViewById(R.id.movie_rating_name);
            mMovieRating = (TextView) rootView.findViewById(R.id.movie_rating);
            mReleaseTitle = (TextView) rootView.findViewById(R.id.movie_release_name);
            mMovieRelease = (TextView) rootView.findViewById(R.id.movie_release);
            mFavButton = (Button) rootView.findViewById(R.id.favorites_button);
            mSynopTitle = (TextView) rootView.findViewById(R.id.movie_synopsis_name);
            mMovieSynop = (TextView) rootView.findViewById(R.id.movie_synopsis);
            mTrailerTitle = (TextView) rootView.findViewById(R.id.title_trailer);
            mMovieTrailers = (ListView) rootView.findViewById(R.id.movie_trailers);
            mReviewTitle = (TextView) rootView.findViewById(R.id.title_review);
            mMovieReviews = (ListView) rootView.findViewById(R.id.movie_reviews);

            // Next, fetch movie information from the passed in struct
            final String movieToFetch = mTheMovie.getMovieId();
            final String movieImage = mTheMovie.getImageURL();
            final String movieTitle = mTheMovie.getMovieTitle();
            final String movieSynop = mTheMovie.getMovieSynop();
            final String movieRating = mTheMovie.getMovieRating();
            final String movieRel = mTheMovie.getMovieRel();
            mShareText = "Title: " + movieTitle + "\n" +
                         "Synopsis: " + movieSynop + "\n" +
                         "Average Rating: " + movieRating + "\n" +
                         "Release Date: " + movieRel + "\n";

            // Now, render every piece of movie info with the corresponding
            // GUI widget
            Picasso.with(getContext()).load(IMAGE_BASE_URL+movieImage).into(mMovieImage);
            mTitleBackground.setBackgroundColor(getResources().getColor(R.color.teal));
            mMovieTitle.setText(movieTitle);
            mRatingTitle.setText(getString(R.string.rating_title_name));
            mMovieRating.setText(movieRating);
            mReleaseTitle.setText(getString(R.string.release_date_name));
            mMovieRelease.setText(parseDateToReadable(movieRel));
            mSynopTitle.setText(getString(R.string.synopsis_name));
            mMovieSynop.setText(movieSynop);

            // Fetch information for the trailers of this particular movie
            new FetchTrailersTask(this).execute(movieToFetch);
            mTrailerTitle.setText(getString(R.string.trailers_name));
            mMovieTrailers.setFocusable(false);
            mTrailerAdapter = new ArrayAdapter<>(
                    getActivity(),
                    R.layout.list_item_trailer,
                    R.id.list_item_trailer_view,
                    new ArrayList<String>());
            mMovieTrailers.setAdapter(mTrailerAdapter);
            mMovieTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String videoId = mTrailerNameAndKey.get(mTrailerAdapter.getItem(position));
                    watchYoutubeVideo(videoId);
                }
            });

            // Fetch information for the reviews of this particular movie
            new FetchReviewsTask(this).execute(movieToFetch);
            mReviewTitle.setText(getString(R.string.reviews_name));
            mMovieReviews.setFocusable(false);
            mReviewAdapter = new ArrayAdapter<>(
                    getActivity(),
                    R.layout.list_item_review,
                    R.id.list_item_review_view,
                    new ArrayList<String>());
            mMovieReviews.setAdapter(mReviewAdapter);
            mMovieReviews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String reviewUrl = mReviewAuthorAndUrl.get(mReviewAdapter.getItem(position));
                    readMovieReview(reviewUrl);
                }
            });

            // Kick off a another task to do database query to determine if movie is already a fav
            new IsButtonAlreadyFavorite().execute(movieToFetch);
            mFavButton.setBackgroundColor(getResources().getColor(R.color.sunshine_light_blue));
            mFavButton.setVisibility(View.VISIBLE);
            mFavButton.setOnClickListener(new AdapterView.OnClickListener() {
                public void onClick(View view) {
                    Toast toast;
                    if (mIsMovieFavorite) {
                        new QueryMovieTask(getActivity()).execute("delete", movieToFetch);
                        toast = Toast.makeText(getContext(), "Movie deleted as a favorite", Toast.LENGTH_SHORT);
                    } else {
                        new QueryMovieTask(getActivity()).execute("add", movieToFetch, IMAGE_BASE_URL + movieImage, movieTitle,
                                movieSynop, movieRating, movieRel);
                        toast = Toast.makeText(getContext(), "Movie saved as a favorite!", Toast.LENGTH_SHORT);
                    }
                    toast.show();
                    // IMPORTANT: make sure to update the button text after the user clicks on it!
                    // After all, a movie shouldn't be added twice as a favorite
                    new IsButtonAlreadyFavorite().execute(movieToFetch);
                }
            });
        }
        return rootView;
    }

    // Function to update trailer and review adapter after being fetched
    public void setMovieInfo(Map<String, String> movieInfo, boolean isTrailerInfo) {
        if (movieInfo.isEmpty()) {
            (isTrailerInfo ? mTrailerTitle : mReviewTitle).setText("");
        }
        (isTrailerInfo ? mTrailerAdapter : mReviewAdapter).clear();
        (isTrailerInfo ? mTrailerAdapter : mReviewAdapter).addAll(movieInfo.keySet().toArray(new String[movieInfo.size()]));
        (isTrailerInfo ? mTrailerNameAndKey : mReviewAuthorAndUrl).clear();
        (isTrailerInfo ? mTrailerNameAndKey : mReviewAuthorAndUrl).putAll(movieInfo);
        setListViewHeightBasedOnChildren((isTrailerInfo ? mMovieTrailers : mMovieReviews));
    }

    // async function to query movie to determine if it is already a favorite
    class IsButtonAlreadyFavorite extends AsyncTask<String, Void, Boolean> {
        private final String LOG_TAG = IsButtonAlreadyFavorite.class.getSimpleName();

        protected Boolean doInBackground(String ...params) {
            Boolean foundId = false;
            String movieID = params[0];
            String []projection = new String[] {MovieContract.MovieEntry.COLUMN_MOVIE_ID};
            String whereClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
            String []selectionArgs = new String[]{movieID};
            Cursor cur = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    projection, whereClause, selectionArgs, null);
            if (cur.moveToFirst()) {
                do {
                    String mId = cur.getString(cur.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                    if (mId.equals(movieID)) {
                        foundId = true;
                        break;
                    }
                } while (cur.moveToNext());
            }
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
            return foundId;
        }

        protected void onPostExecute(Boolean foundId) {
            mIsMovieFavorite = foundId;
            mFavButton.setText(mIsMovieFavorite ? mContext.getString(R.string.favorite_button_name_delete) : mContext.getString(R.string.favorite_button_name_mark));
        }
    }
}
