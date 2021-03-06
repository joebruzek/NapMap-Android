package com.bruzek.jbruzek.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class LocationActivity extends Activity implements ParseCallbacks {

    private ParseHelper ph;
    private TextView title;
    private TextView description;
    private TextView fullness;
    private RatingBar rating;
    private ListView listView;
    private GoogleMap map;
    private MapView mapView;
    private Button rate;
    private Button comment;
    private Button napHere;
    private Button moreComments;
    private Location l;
    private ArrayList<String> comments;
    private String androidId;
    private String napId;
    private int numComments = 10;

    @Override
    public void onBackPressed() {
        ph.fixRating(napId);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_main);

        androidId = Settings.Secure.getString(this.getContentResolver(),
                                Settings.Secure.ANDROID_ID);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview_header);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        MapsInitializer.initialize(this);

        title = (TextView)findViewById(R.id.location_title);
        description = (TextView)findViewById(R.id.location_description);
        rating = (RatingBar)findViewById(R.id.ratingBar);
        fullness = (TextView)findViewById(R.id.location_fullness);
        listView = (ListView)findViewById(R.id.comments);
        listView.setFocusable(false);

        ph = new ParseHelper(this);

        Intent intent = getIntent();
        napId = intent.getStringExtra("id");
        ph.queryById(napId);
        ph.queryFullness(napId);
        ph.queryComments(napId, numComments);
        ph.fixRating(napId);
        String nap_title = intent.getStringExtra("title");
        title.setText(nap_title);
        setTitle(nap_title);
        getActionBar().setDisplayShowHomeEnabled(false);

        rate = (Button)findViewById(R.id.rate_location);
        final Context context = this;
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder((Activity)context);
                builder.setTitle("Rate");
                // Get the layout inflater
                LayoutInflater inflater = getLayoutInflater();
                final View view = inflater.inflate(R.layout.rate_dialog, null);
                builder.setView(view);
                // Add action buttons
                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        RatingBar rate = (RatingBar) view.findViewById(R.id.rate_dialog_bar);
                        ph.rateNap(l.id(), (int) rate.getRating(), androidId);
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //nothing????
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        comment = (Button)findViewById(R.id.comment_location);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder((Activity)context);
                builder.setTitle("Comment");
                // Get the layout inflater
                LayoutInflater inflater = getLayoutInflater();
                final View view = inflater.inflate(R.layout.comment_dialog, null);
                builder.setView(view);
                        // Add action buttons
                        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText com = (EditText) view.findViewById(R.id.dialog_text);
                                ph.submitComment(com.getText().toString(), l.id());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //nothing????
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        napHere = (Button) findViewById(R.id.nap_here);
        napHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder((Activity)context);
                builder.setMessage("Choosing \"Nap Here\" will show others that someone is currently napping here for the next hour.")
                        .setTitle("Nap Here!")
                        .setPositiveButton("Nap Here!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String android_id = Settings.Secure.getString(context.getContentResolver(),
                                        Settings.Secure.ANDROID_ID);
                                ph.addNap(l.id(), android_id);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //literally nothing
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        moreComments = (Button)findViewById(R.id.add_more_comments);
        moreComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numComments += 10;
                ph.queryComments(l.id(), numComments);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        finish();
        return true;
    }

    @Override
    public void complete() {
        l = ph.currentLocation();
        Log.d("Got location", "Description: " + l.description());
        description.setText(l.description());
        rating.setRating((float) l.rating());
        map.addMarker(new MarkerOptions().position(new LatLng(l.latitude(), l.longitude())).title(l.title()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pillow_pin)));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(l.latitude(), l.longitude()), 17);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void commentsComplete() {
        comments = ph.getComments();
        Log.d("comments Complete", "Size: " + comments.size());
        String[] cList = new String[comments.size()];
        for (int i = 0; i < comments.size(); i++) {
            cList[i] = comments.get(i);
        }
        listView.setAdapter(null);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cList);
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        if (numComments > 14) {
            listView.setSelection(numComments - 14);
        }
    }

    @Override
    public void completeFullness(int full) {
        if (full == 1) fullness.setText(full + " person napping here now");
        else fullness.setText(full + " people napping here now");
    }

    @Override
    public void error() {
        Toast.makeText(this, "You've already rated this location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
