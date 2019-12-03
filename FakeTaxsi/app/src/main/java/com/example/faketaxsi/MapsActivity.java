package com.example.faketaxsi;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.transition.AutoTransition;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.Visibility;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faketaxsi.Database.DatabaseInit;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private DatabaseInit myDb;

    private final String TAG = "Pagrindinis";

    private TextView est_price, est_dur, est_dist;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;

    private Marker destinationMarker, taxiMarker;
    private LatLng userLocation, destLocation;
    private Driver currentDriver;

    private RoutingEngine routingEngine;

    private boolean routeSelected, going;
    private RouteDetails routeDetails;

    private ArrayList<Driver> drivers;
    private ArrayList<LatLng> pointsA, pointsB;

    private long start;
    private static LinearInterpolator interpolator;

    public int currentPoint = 0;
    private boolean letsMove = false;
    private boolean lastAlert = false;
    private boolean loggedIn = false;

    private double lastDist = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        interpolator = new LinearInterpolator();
        myDb = new DatabaseInit(this);
        //myDb.deleteDatabase();
        //myDb.populateDatabase();
        drivers = myDb.readData();

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

        est_price = findViewById(R.id.est_price);
        est_dist = findViewById(R.id.est_dist);
        est_dur = findViewById(R.id.est_dur);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if(loggedIn) {
                if (!routeSelected)
                    Snackbar.make(v, "No route selected!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                else if (!going) {
                    fab.setImageResource(R.drawable.ic_autorenew);
                    fab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
                    going = true;
                    startTaxiService(fab);
                } else {
                    if (taxiMarker != null)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taxiMarker.getPosition(), 14));
                }
            }
            else
                Snackbar.make(v, "You must Log in!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        });

        findViewById(R.id.logIn).setOnClickListener(v -> {
            loggedIn = true;
            Snackbar.make(v, "Logged In!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            v.setVisibility(View.GONE);
        });
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
            if(!going) {
                mMap.clear();
                mapClicked(point);
            }
        });
        destinationMarker = null;

        checkPremission();
    }

    private void startTaxiService(FloatingActionButton fab){
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (drivers != null) {
                int driver = new Random().nextInt(drivers.size());
                taxiMarker = mMap.addMarker(new MarkerOptions().position(drivers.get(driver).getLoc())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_icon2))
                        .anchor(0.5f, 0.5f));
                currentDriver = drivers.get(driver);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taxiMarker.getPosition(), 14));
                calcRoute(taxiMarker.getPosition(), userLocation, false);
                letsMove = true;

                fab.clearAnimation();
                fab.setImageResource(R.drawable.ic_taxi);
            }
        }, 3000);
    }

    public void checkPremission(){
        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // reuqest for permission
            int locationRequestCode = 1000;
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
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Toast.makeText(this, "Location: " + userLocation, Toast.LENGTH_SHORT).show();
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
        //toggle();

        if(userLocation != null) {
            calcRoute(userLocation, destLocation, true);
        }
    }

    private void moveMarker(ArrayList<LatLng> points){
        start = SystemClock.uptimeMillis();
        currentPoint = 0;
        letsMove = true;
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                moveAtoB(points);
                if(letsMove)
                    handler.postDelayed(this, 10);
                else
                    handler.removeCallbacksAndMessages(null);
            }
        };
        handler.postDelayed(r, 10);

    }

    private void moveAtoB(ArrayList<LatLng> points){
        long elapsed = SystemClock.uptimeMillis() - start;
        double t = interpolator.getInterpolation((float)elapsed/100);

        double lat = t * points.get(currentPoint+1).latitude + (1-t) * points.get(currentPoint).latitude;
        double lng = t * points.get(currentPoint+1).longitude + (1-t) * points.get(currentPoint).longitude;

        LatLng intermediatePosition = new LatLng(lat, lng);
        double dist = distanceAtoB(points.get(currentPoint+1), intermediatePosition);
        if(lastDist == 0)
            lastDist = dist;

        //Log.v("inform", "Dist: " + dist + " LastDist: " + lastDist);
        if(dist > lastDist) {
            taxiMarker.setPosition(points.get(currentPoint+1));
            currentPoint++;
            start = SystemClock.uptimeMillis();
            lastDist = 0;
        }
        else {
            taxiMarker.setPosition(intermediatePosition);
            lastDist = dist;
        }

        if(currentPoint+1 >= points.size()) {
            letsMove = false;
            carArrivedToUser();
            lastAlert = true;
        }
    }

    private double distanceAtoB(LatLng start, LatLng finish){
        Location locationA = new Location("point A");
        locationA.setLatitude(start.latitude);
        locationA.setLongitude(start.longitude);
        Location locationB = new Location("point B");
        locationB.setLatitude(finish.latitude);
        locationB.setLongitude(finish.longitude);

        return locationA.distanceTo(locationB);
    }

    private void calcRoute(LatLng start, LatLng finish, boolean show){
        Waypoint startWaypoint = new Waypoint(new GeoCoordinates(start.latitude, start.longitude));
        Waypoint destinationWaypoint = new Waypoint(new GeoCoordinates(finish.latitude, finish.longitude));

        List<Waypoint> waypoints =
                new ArrayList<>(Arrays.asList(startWaypoint, destinationWaypoint));

        routingEngine.calculateRoute(
                waypoints,
                new CarOptions(),
                (routingError, routes) -> {
                    if (routingError == null) {
                        Route route = routes.get(0);
                        if(show) {
                            showRouteDetails(route);
                            drawPolyline(route, Color.RED);
                        }
                        else
                            drawPolyline(route, Color.GREEN);

                        routeSelected = true;
                    } else {
                        Log.d(TAG, routingError.toString());
                    }
                });
    }

    private void drawPolyline(Route route, int cl){
        GeoPolyline routeGeoPolyline;
        try {
            routeGeoPolyline = new GeoPolyline(route.getShape());
        } catch (InstantiationErrorException e) {
            return;
        }

        ArrayList<LatLng> points = new ArrayList<>();
        PolylineOptions lineOptions = new PolylineOptions();

        for (GeoCoordinates pos : routeGeoPolyline.vertices) {
            LatLng position = new LatLng(pos.latitude, pos.longitude);
            points.add(position);
        }

        //Log.v("inform", "End: " + points.get(points.size()-1));
        lineOptions.addAll(points);
        lineOptions.width(12);
        lineOptions.color(cl);
        lineOptions.geodesic(true);

        // Drawing polyline in the Google Map for the i-th route
        mMap.addPolyline(lineOptions);

        if(cl == Color.RED)
            pointsA = points;
        else
            pointsB = points;

        if(letsMove)
            moveMarker(pointsB);
    }

    private void showRouteDetails(Route route){
        routeDetails = new RouteDetails((route.getLengthInMeters()/1000f)*1f, route.getLengthInMeters(), route.getBaseTimeInSeconds()/60f);

        est_price.setText("Estimated price: " + String.format("%.02f", routeDetails.getPrice()) + "Eur");
        est_dist.setText("Distance: " + routeDetails.getDistance() + "m");
        est_dur.setText("Duration: " + String.format("%.02f", routeDetails.getDuration()) + "min");

        //Toast.makeText(this, "Distance: " + route.getLengthInMeters() + "m Time: " + route.getBaseTimeInSeconds() + "s" , Toast.LENGTH_LONG).show();
    }

    private void carArrivedToUser(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set dialog message
        if(!lastAlert){
            alertDialogBuilder
                    .setTitle("Car Arrived!")
                    .setMessage(new DriverReader(currentDriver).getDriverData())
                    .setCancelable(false)
                    .setPositiveButton("Lets Roll!", (dialog, id) -> {
                        goToDestination();
                        dialog.cancel();
                    }).create().show();}
        else{
            alertDialogBuilder
                    .setTitle("You Have Arrived To Your Destination!")
                    .setMessage(
                            "Price: " + String.format("%.02f", routeDetails.getPrice()) + "Eur\n" +
                            "Distance: " + routeDetails.getDistance() + "m\n" +
                            "Duration: " + String.format("%.02f", routeDetails.getDuration()) + "min\n")
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> {
                        //goToDestination();
                        savehistory();
                        dialog.cancel();
                    }).create().show();
        }
    }

    private void goToDestination(){
        moveMarker(pointsA);
    }

    private void savehistory(){
        routeSelected = false;
        going = false;
        lastAlert = false;

        myDb.insertHistory(routeDetails.getDistance(), routeDetails.getDuration(), routeDetails.getPrice());

        Intent myIntent = new Intent(this, HistoryActivity.class);
        this.startActivity(myIntent);
    }
}
