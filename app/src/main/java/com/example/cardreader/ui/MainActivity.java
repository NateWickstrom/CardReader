package com.example.cardreader.ui;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.cardreader.App;
import com.example.cardreader.R;
import com.example.cardreader.io.CardsRequest;
import com.example.cardreader.model.Card;
import com.example.cardreader.model.MovieCard;
import com.example.cardreader.model.MusicCard;
import com.example.cardreader.model.PlaceCard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;


public class MainActivity extends ActionBarActivity {

    /* buffers for storing fetched info */
    final Capture<List<Card>> mCardsListBuffer = new Capture<>();
    final Capture<Location> mLocationBuffer = new Capture<>();

    private List<Card> mCardsList = new ArrayList<>();

    private LayoutInflater mInflater;
    private CardsAdapter mAdapter;
    private TextView mTextView;
    private ListView mListView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mAdapter = new CardsAdapter();

        mListView = (ListView) findViewById(android.R.id.list);
        mProgressView = findViewById(R.id.progress_bar);

        mTextView = (TextView) mInflater.inflate(R.layout.list_header, mListView, false);

        mListView.addHeaderView(mTextView);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchInfo();
    }

    @Override
    protected void onStop() {
        //todo, background tasks should be cancelled, if running...
        super.onStop();
    }

    private void fetchInfo(){

        // set 'loading screen' view
        mListView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);

        // clear local data
        mCardsList.clear();
        mAdapter.notifyDataSetChanged();

        Task.forResult(null).continueWithTask(new Continuation<Object, Task<Void>>() {
            public Task<Void> then(Task<Object> results) throws Exception {

                ArrayList<Task<Void>> tasks = new ArrayList<>();
                tasks.add(getCardsFetcherTask());
                tasks.add(getLocationTask());
                // additional tasks can be added here...

                // waits until all tasks have completed (in parallel) before returning
                return Task.whenAll(tasks);
            }
        }).continueWith(new Continuation<Void, Void>() {
            public Void then(Task<Void> ignored) throws Exception {
                //todo, error checking is skipped...

                mCardsList.addAll(mCardsListBuffer.get());
                mAdapter.notifyDataSetChanged();

                Location location = mLocationBuffer.get();
                String locationString = location.getLongitude() + ", " + location.getLatitude();
                mTextView.setText(locationString);

                mListView.setVisibility(View.VISIBLE);
                mProgressView.setVisibility(View.GONE);

                return null;
            }
        });

    }

    private Task<Void> getCardsFetcherTask() {
        final Task<Void>.TaskCompletionSource tcs = Task.create();

        App.getQueue().add(new CardsRequest(
                new Response.Listener<List<Card>>() {
                    @Override
                    public void onResponse(List<Card> cards) {
                        mCardsListBuffer.set(cards);
                        tcs.setResult(null);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        tcs.setError(volleyError);
                    }
                }));

        return tcs.getTask();
    }

    private Task<Void> getLocationTask() {
        final Task<Void>.TaskCompletionSource tcs = Task.create();
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                mLocationBuffer.set(location);
                locationManager.removeUpdates(this);
                tcs.setResult(null);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        return tcs.getTask();
    }

    private class CardsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCardsList.size();
        }

        @Override
        public Card getItem(int position) {
            return mCardsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Card card = getItem(position);
            view = mInflater.inflate(R.layout.list_item_card, parent, false);

            final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.card_root);
            final ImageView thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            final TextView titleTextView = (TextView) view.findViewById(R.id.card_title);

            final String title = card.getTitle();
            final String imageUrl = card.getImageUrl();

            // Set the Title
            titleTextView.setText(title);

            // Set the Thumbnail
            Picasso.with(parent.getContext())
                    .load(imageUrl)
                    .error(R.drawable.ic_no_image_available)
                    .into(thumbnailView);

            // now set specific views
            switch(card.getType()) {
                case "place":
                    PlaceCard placeCard = (PlaceCard) card;
                    TextView textView = (TextView) mInflater.inflate(R.layout.list_item_card_place, parent, false);
                    textView.setText(placeCard.getPlaceCategory());
                    linearLayout.addView(textView);
                    break;
                case "movie":
                    MovieCard movieCard = (MovieCard) card;
                    ImageView imageView = (ImageView) mInflater.inflate(R.layout.list_item_card_movie, parent, false);
                    Picasso.with(parent.getContext())
                            .load(movieCard.getMovieExtraImageUrl())
                            .error(R.drawable.ic_no_image_available)
                            .into(imageView);
                    linearLayout.addView(imageView);
                    break;
                case "music":
                    MusicCard musicCard = (MusicCard) card;
                    final String url = musicCard.getMusicVideoUrl();
                    Button button = (Button) mInflater.inflate(R.layout.list_item_card_music, parent, false);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    });
                    linearLayout.addView(button);
                    break;
            }

            return view;
        }
    }

}
