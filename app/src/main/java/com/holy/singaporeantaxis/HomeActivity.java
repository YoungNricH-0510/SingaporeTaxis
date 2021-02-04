package com.holy.singaporeantaxis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.holy.singaporeantaxis.helpers.UtilHelper;
import com.holy.singaporeantaxis.helpers.SQLiteHelper;
import com.holy.singaporeantaxis.helpers.TaxiApiHelper;
import com.holy.singaporeantaxis.models.Taxi;
import com.holy.singaporeantaxis.models.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class HomeActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    public static final String EXTRA_SEARCHED_USER_ID = "com.holy.singaporeantaxis.searchedUserId";

    public static final int REQUEST_LOCATION_PERMISSION = 100;
    public static final int REQUEST_CALL_PHONE_PERMISSION = 101;
    public static final String DEFAULT_LOCATION_NAME = "Marina bay";
    public static final float DEFAULT_ZOOM_LEVEL = 17.0f;
    public static final int DEFAULT_TAXI_RANGE_IN_METERS = 500;

    private Toolbar mToolbar;
    private String mCurrentId;

    private GoogleMap mGoogleMap;
    private List<Marker> mTaxiMarkerList;
    private List<Marker> mUserMarkerList;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private View mUserInfoView;
    private User mSelectedUser;
    private Polyline mConnectingLine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Check current id
        mCurrentId = ((App) getApplication()).getCurrentId();
        if (mCurrentId == null) {
            Toast.makeText(this,
                    "Sorry, you are not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize google map fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Create fused location provider client for location updates
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Check location permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {

            // Request location permission
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Get views
        mUserInfoView = findViewById(R.id.user_info_view);

        // Create marker lists
        mTaxiMarkerList = new ArrayList<>();
        mUserMarkerList = new ArrayList<>();

        // Initialize toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onResume() {

        super.onResume();

        // Start location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }

        // Start thread for loading taxi data
        mStopLoadTaxiData = false;
        mLoadTaxiDataThread = new Thread(mLoadTaxiData);
        mLoadTaxiDataThread.start();
    }

    @Override
    public void onPause() {

        // Stop location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            stopLocationUpdates();
        }

        // Stop thread for loading taxi data
        mStopLoadTaxiData = true;
        try {
            mLoadTaxiDataThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (mSelectedUser != null) {
            // Un-select the currently selected user
            mSelectedUser = null;
            hideUserInformation();
        } else {
            // Ask whether to sign out and finish activity
            new AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to sign out?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                        // Sign out
                        signOut();

                        // Finish activity
                        super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_home, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.item_search).getActionView();
        searchView.setSubmitButtonEnabled(true);

        // Set searchable info of search view
        ComponentName searchableActivityCompName = ComponentName.createRelative(
                getPackageName(), SearchableActivity.class.getName());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(searchableActivityCompName));
        searchView.setIconifiedByDefault(true);

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_calculate:

                break;
            case R.id.item_settings:

                break;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission Granted: start location updates
                    startLocationUpdates();
                } else {
                    // Location permission Denied: just move camera to default location
                    moveCameraToDefaultLocation();
                }
                break;
            case REQUEST_CALL_PHONE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Call Phone permission Granted: Start phone call activity
                    if (mSelectedUser != null) {
                        String tel = String.format(Locale.getDefault(),
                                "tel:%s", mSelectedUser.getPhone());
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel));
                        startActivity(intent);
                    }
                } else {
                    // Call phone permission Denied: Show a toast message
                    Toast.makeText(this,
                            "Permission required to make a phone call", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // Process signing out : Must called before activity is paused

    private void signOut() {

        // - Update the signed state
        SQLiteHelper.getInstance(this).updateUserSignedState(mCurrentId, false);
        // - Set current id of application to null
        ((App) getApplication()).setCurrentId(null);

    }

    // Start location updates

    private void startLocationUpdates() {

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            LocationRequest locationRequest = new LocationRequest();
            mFusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, mLocationCallback, Looper.getMainLooper());
        }
    }

    // Stop location updates

    private void stopLocationUpdates() {

        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    // Google map ready

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Initialize google map
        mGoogleMap = googleMap;
        mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));

        // - Add zoom controls and My Location button
        UiSettings uiSettings = mGoogleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        // - Set Marker click listener
        mGoogleMap.setOnMarkerClickListener(this);

        // - Set Map Click listener : Hide User Info View if showing
        mGoogleMap.setOnMapClickListener(latLng -> {
            if (mUserInfoView.getVisibility() == View.VISIBLE) {
                mSelectedUser = null;
                hideUserInformation();
            }
        });

        // If the app doesn't have location permission, move camera to the default location
        if (getIntent().hasExtra(EXTRA_SEARCHED_USER_ID)) {
            String searchedUserId = getIntent().getStringExtra(EXTRA_SEARCHED_USER_ID);
            mSelectedUser = SQLiteHelper.getInstance(this).getUser(searchedUserId);
            if (mSelectedUser != null) {
                displayUserInformation();
            }
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {
            moveCameraToDefaultLocation();
        }
    }

    private void moveCameraToDefaultLocation() {

        if (mGoogleMap == null) {
            return;
        }

        // Get position of default location using Geocoder
        Geocoder geocoder = new Geocoder(this);

        try {
            Address address = geocoder.getFromLocationName(
                    DEFAULT_LOCATION_NAME, 1).get(0);

            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            // Move camera to the position
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);

            mGoogleMap.moveCamera(cameraUpdate);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update taxi UI

    private void updateTaxiUIs(List<Taxi> taxiList) {

        updateTaxiMarkers(taxiList);
        updateTaxiCount(taxiList);
    }

    // Update taxi markers

    private void updateTaxiMarkers(List<Taxi> taxiList) {

        if (mGoogleMap == null) {
            return;
        }

        // Remove all existing markers
        for (int i = mTaxiMarkerList.size() - 1; i >= 0; i--) {
            mTaxiMarkerList.get(i).remove();
            mTaxiMarkerList.remove(i);
        }

        // Display taxis within range as markers
        for (Taxi taxi : taxiList) {

            if (!isLocationWithinRange(taxi.getLocation())) {
                continue;
            }

            // Create marker for each taxi
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(taxi.getLocation())
                    .icon(UtilHelper.bitmapDescriptorFromVector(this, R.drawable.marker_taxi))
                    .visible(true);

            Marker marker = mGoogleMap.addMarker(markerOptions);
            mTaxiMarkerList.add(marker);
        }
    }

    // Update taxi count

    private void updateTaxiCount(List<Taxi> taxiList) {

        if (mGoogleMap == null) {
            return;
        }

        int taxiCount = 0;

        for (Taxi taxi : taxiList) {

            if (isLocationWithinRange(taxi.getLocation())) {
                taxiCount++;
            }
        }

        String strTaxiCount = String.format(Locale.getDefault(),
                "%d taxis available", taxiCount);

        mToolbar.setTitle(strTaxiCount);
    }

    // Check if given taxi is within range

    private boolean isLocationWithinRange(LatLng location) {

        // Get current camera position
        CameraPosition cameraPosition = mGoogleMap.getCameraPosition();
        if (cameraPosition == null) {
            return false;
        }

        float[] results = new float[3];
        Location.distanceBetween(
                cameraPosition.target.latitude, cameraPosition.target.longitude,
                location.latitude, location.longitude,
                results
        );

        return (results[0] <= DEFAULT_TAXI_RANGE_IN_METERS);
    }

    // Update person markers

    private void updateUserMarkers() {

        if (mGoogleMap == null) {
            return;
        }

        // Remove all existing markers
        for (int i = mUserMarkerList.size() - 1; i >= 0; i--) {
            mUserMarkerList.get(i).remove();
            mUserMarkerList.remove(i);
        }

        // Get locations of users currently signed in
        List<User> userList = SQLiteHelper.getInstance(this).getUsersSignedIn();

        // Display all users signed in as markers
        for (User user : userList) {

            if (user.getLastLocation() == null) {
                continue;
            }

            // Marker shows different color if it's users
            int bitmapRes = (user.getId().equals(mCurrentId) ?
                    R.drawable.ic_marker_person_highlighted :
                    R.drawable.ic_marker_person);

            // Create marker for each user
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(user.getLastLocation())
                    .icon(UtilHelper.bitmapDescriptorFromVector(this, bitmapRes))
                    .visible(true)
                    .title(user.getId());

            Marker marker = mGoogleMap.addMarker(markerOptions);
            mUserMarkerList.add(marker);
        }
    }

    // Process user marker click

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (mSelectedUser != null) {
            hideUserInformation();
        }

        String clickedUserId = marker.getTitle();
        if (clickedUserId == null) {
            return false;
        }

        // Clicked myself
        if (clickedUserId.equals(mCurrentId)) {
            return false;
        }

        // Clicked marker that is not user's
        mSelectedUser = SQLiteHelper.getInstance(this).getUser(clickedUserId);
        if (mSelectedUser == null) {
            return false;
        }

        // Display user information
        displayUserInformation();

        return false;
    }

    // Display user information

    private void displayUserInformation() {

        // No user to show information
        if (mSelectedUser == null) {
            return;
        }

        // Display user information on user info view
        TextView userIdText = mUserInfoView.findViewById(R.id.txt_user_id);
        TextView userPhoneText = mUserInfoView.findViewById(R.id.txt_user_phone);

        userIdText.setText(mSelectedUser.getId());
        userPhoneText.setText(mSelectedUser.getPhone());

        // Set call button click listener
        Button callButton = mUserInfoView.findViewById(R.id.button_call);
        callButton.setOnClickListener(v -> {

            // Check Call Phone permission before starting phone call activity
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) ==
                    PackageManager.PERMISSION_GRANTED) {

                // Granted: Start phone call activity
                String tel = String.format(Locale.getDefault(), "tel:%s", mSelectedUser.getPhone());
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel));
                startActivity(intent);
            } else {

                // Denied: Request the permission
                requestPermissions(
                        new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_CALL_PHONE_PERMISSION);
            }
        });

        // Make the view (currently gone) visible with an animation
        mUserInfoView.setVisibility(View.VISIBLE);
        mUserInfoView.startAnimation(
                AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));

        // Draw a polyline connecting the user and me on the map
        if (mGoogleMap != null) {
            User me = SQLiteHelper.getInstance(this).getUser(mCurrentId);

            // Define pattern of polyline
            final PatternItem DOT = new Dot();
            final PatternItem GAP = new Gap(10);
            final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(0xFF3333AA)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .pattern(PATTERN_POLYLINE_DOTTED)
                    .add(me.getLastLocation())
                    .add(mSelectedUser.getLastLocation());

            mConnectingLine = mGoogleMap.addPolyline(polylineOptions);

            // Move camera to the position of selected user
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLng(mSelectedUser.getLastLocation());
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    // Hide user information

    private void hideUserInformation() {

        // Apply slide-out animation
        Animation slideOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        // Make the view gone after the animation is finished
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mUserInfoView.setVisibility(View.GONE);
                mSelectedUser = null;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mUserInfoView.startAnimation(slideOut);

        // Hide the polyline from the map
        if (mConnectingLine != null) {
            mConnectingLine.remove();
            mConnectingLine = null;
        }
    }

    // Background thread for loading taxi data

    private Thread mLoadTaxiDataThread;

    private final Runnable mLoadTaxiData = new Runnable() {
        @Override
        public void run() {
            while (!mStopLoadTaxiData) {

                // Load taxi data
                TaxiApiHelper taxiApiHelper = new TaxiApiHelper(HomeActivity.this);
                taxiApiHelper.loadTaxiData(taxiList -> {

                    // Update taxi markers on the map
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> updateTaxiUIs(taxiList));
                });

                // Give delay of 10000ms
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean mStopLoadTaxiData;

    // Location callback

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            // Move camera to the updated location
            if (locationResult == null || mGoogleMap == null) {
                return;
            }

            // Get last location
            Location location = locationResult.getLastLocation();
            if (location == null) {
                return;
            }

            LatLng latLng = new LatLng(
                    location.getLatitude(), location.getLongitude());

            // Move camera to the location
            if (mSelectedUser == null) {

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);

                mGoogleMap.moveCamera(cameraUpdate);
            }

            // Update my location in DB
            SQLiteHelper.getInstance(HomeActivity.this)
                    .updateUserLastLocation(mCurrentId, latLng);

            // Update person markers on the map
            updateUserMarkers();
        }
    };

}