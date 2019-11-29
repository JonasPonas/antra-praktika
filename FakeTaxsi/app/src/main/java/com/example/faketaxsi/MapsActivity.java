package com.example.faketaxsi;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.errors.EngineInstantiationException;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.routing.CarOptions;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.Waypoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "Pagrindinis";

    private ViewGroup mainLayout;
    private ViewGroup extraInfo;

    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;

    private Marker destinationMarker;
    private LatLng userLocation, destLocation;

    private RoutingEngine routingEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(isGooglePlayServicesAvailable())
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            routingEngine = new RoutingEngine();
        } catch (EngineInstantiationException e) {
            new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }

        mainLayout = findViewById(R.id.mainLayout);
        extraInfo = findViewById(R.id.extraInfo);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(point -> {
            // TODO Auto-generated method stub
            mMap.clear();
            mapClicked(point);
        });
        destinationMarker = null;

        checkPremission();
    }

    public void checkPremission(){
        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // reuqest for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        } else {
            // already permission granted
            getLocation();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    public void getLocation(){
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                wayLatitude = location.getLatitude();
                wayLongitude = location.getLongitude();
                //txtLocation.setText(String.format(Locale.US, "%s -- %s", wayLatitude, wayLongitude));
                Toast.makeText(this, "Location " + wayLatitude + " " + wayLongitude, Toast.LENGTH_SHORT).show();

                userLocation = new LatLng(wayLatitude, wayLongitude);
                //userLocation = new LatLng(54.739010f, 25.226059f);
                //mMap.addMarker(new MarkerOptions().position(userLocation).title("Current"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
            }
        });
    }
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(this, "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void mapClicked(LatLng point){
        destinationMarker = mMap.addMarker(new MarkerOptions().position(point));
        destLocation = destinationMarker.getPosition();
        toggle();

        if(userLocation != null) {
            calcRoute();
        }
    }

    private void toggle() {
        Transition transition = new Fade(Fade.IN);
        transition.setDuration(6000);
        transition.addTarget(R.id.extraInfo);

        TransitionManager.beginDelayedTransition(mainLayout, transition);
        extraInfo.setVisibility(View.VISIBLE);
    }

    private void calcRoute(){
        Waypoint startWaypoint = new Waypoint(new GeoCoordinates(userLocation.latitude, userLocation.longitude));
        Waypoint destinationWaypoint = new Waypoint(new GeoCoordinates(destLocation.latitude, destLocation.longitude));

        List<Waypoint> waypoints =
                new ArrayList<>(Arrays.asList(startWaypoint, destinationWaypoint));

        routingEngine.calculateRoute(
                waypoints,
                new CarOptions(),
                (routingError, routes) -> {
                    if (routingError == null) {
                        Route route = routes.get(0);
                        showRouteDetails(route);
                        drawPolyline(route);
                    } else {
                        Log.d(TAG, routingError.toString());
                    }
                });
    }

    private void drawPolyline(Route route){
        GeoPolyline routeGeoPolyline;
        try {
            routeGeoPolyline = new GeoPolyline(route.getShape());
        } catch (InstantiationErrorException e) {
            return;
        }

        ArrayList points = new ArrayList();
        PolylineOptions lineOptions = new PolylineOptions();

        for (GeoCoordinates pos : routeGeoPolyline.vertices) {
            LatLng position = new LatLng(pos.latitude, pos.longitude);
            points.add(position);
        }

        lineOptions.addAll(points);
        lineOptions.width(12);
        lineOptions.color(Color.RED);
        lineOptions.geodesic(true);

        // Drawing polyline in the Google Map for the i-th route
        mMap.addPolyline(lineOptions);
    }

    private void showRouteDetails(Route route){
        Toast.makeText(this, "Distance: " + route.getLengthInMeters() + "m Time: " + route.getBaseTimeInSeconds() + "s" , Toast.LENGTH_LONG).show();
    }
}
