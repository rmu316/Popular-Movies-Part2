package com.example.richardmu.flixbrite.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements PosterFragment.Callback {
    // Globals
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    // Member Variables
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(MovieData theMovie) {
        if (mTwoPane) {
            startTwoPaneTranscation(theMovie);
        } else {
            // in the single-pane mode, we just do what we'd normally do -
            // we simply place the movie data struct into the intent
            // for the detailed page
            Intent intent = new Intent(this, DetailActivity.class)
                .putExtra(Intent.EXTRA_TEXT, theMovie);
            startActivity(intent);
        }
    }

    @Override
    public void displayFirstItem(MovieData theMovie) {
        if (mTwoPane) {
            startTwoPaneTranscation(theMovie);
        }
    }

    private void startTwoPaneTranscation(MovieData theMovie) {
        // In two-pane mode, show the detail view in this activity by
        // adding or replacing the detail fragment using a
        // fragment transaction.
        Bundle args = new Bundle();
        args.putParcelable(DetailActivityFragment.DETAIL_URI, theMovie);
        DetailActivityFragment fragment = new DetailActivityFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                .commit();
    }
}
