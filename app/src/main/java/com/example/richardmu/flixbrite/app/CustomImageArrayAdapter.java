package com.example.richardmu.flixbrite.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Richard Mu on 5/4/2016.
 */
public class CustomImageArrayAdapter extends ArrayAdapter<String> {
    // Globals
    private static final String LOG_TAG = CustomImageArrayAdapter.class.getSimpleName();

    // Members
    private Context mContext;
    private int mResourceId;
    ArrayList<String> mImageUrls = new ArrayList<>();

    public CustomImageArrayAdapter(Activity context, int resource, ArrayList<String> urls) {
        super(context, resource, urls);
        this.mContext = context;
        this.mResourceId = resource;
        this.mImageUrls.addAll(urls);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mResourceId, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(convertView);
        String url = getItem(position);
        Picasso.with(mContext).load(url).placeholder(R.drawable.loading_indicator).error(R.drawable.not_found).into(viewHolder.imageView);
        convertView.setTag(viewHolder);
        return convertView;
    }

    public void setMovieData(ArrayList<String> imageUrls) {
        this.mImageUrls.clear();
        this.mImageUrls.addAll(imageUrls);
        notifyDataSetChanged();
    }

    /**
     * Cache of the children views for this adapter.
     * Makes loading/reloading images easier
     */
    public static class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.grid_item_poster_imageview);
        }
    }
}
