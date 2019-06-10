package com.example.googlemapsapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;
    private boolean gotMyLocationOneTime;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private double latitude, longitude;
    private double previousLatitude, previousLongitude;
    private boolean notTrackingMyLocation;
    private int trackMarkerDropCounter = 0;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOCATION_ZOOM_FACTOR = 17;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng pasadena = new LatLng(34.148, -118.145);
        mMap.addMarker(new MarkerOptions().position(pasadena).title("Born Here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pasadena));



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Permission Check 1");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Permission Check 2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Dropping marker at my location");
            mMap.setMyLocationEnabled(true);
        }

        locationSearch = (EditText) findViewById(R.id.editText_addr);

        gotMyLocationOneTime = false;
        getLocation();
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //Get GPS Status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMapsApp", "getLocation: GPS is enabled");

            //Get network status (cell tower + wifi)
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMapsApp", "getLocation: Network is enabled");


            if (!isGPSEnabled && !isNetworkEnabled) //no provider is enabled
                Log.d("MyMapsApp", "getLocation: no provider is enabled");
            else {
                if (isNetworkEnabled) {
                    Log.d("MyMapsApp", "getLocation: network provider is enabled");
                    Toast.makeText(this,"getLocation: Network Enabled",Toast.LENGTH_SHORT).show();

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                }
                if (isGPSEnabled) {
                    Log.d("MyMapsApp", "getLocation: GPS provider is enabled");
                    Toast.makeText(this,"getLocation: GPS Enabled",Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);

                }
            }
        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation: Printing stack trace");
            e.printStackTrace();
        }
    }

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMapsApp","locationListenerNetwork - onLocationChanged dropping marker");
            dropMarker(LocationManager.NETWORK_PROVIDER);

            if(gotMyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                gotMyLocationOneTime = true;

                //Calculate distance?
            }
            if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerNetwork);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp","LocationListenerNetwork: onStatusChanged callback");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMapsApp","LocationListenerNetwork - onLocationChanged: dropping marker");
            Toast.makeText(MapsActivity.this, "locationListenerGPS: Using GPS", Toast.LENGTH_SHORT).show();

            dropMarker(LocationManager.GPS_PROVIDER);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            switch (status) {
                case LocationProvider
                        .AVAILABLE:
                    Log.d("MyMapsApp","locationListenerGPS - onStatusChanged - AVAIL");
                    Toast.makeText(MapsActivity.this, "locationListenerGPS: Available", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMapsApp","locationListenerGPS - onStatusChanged - OUTOFSERVICE");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MapsActivity.this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMapsApp","locationListenerGPS - onStatusChanged - TEMP UNAVAIL");
                    Toast.makeText(MapsActivity.this, "locationListenerGPS: Temp Unavailable", Toast.LENGTH_SHORT).show();

                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MapsActivity.this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);


                    break;
                default:
                    Log.d("MyMapsApp","locationListenerGPS - onStatusChanged - default");
                    Toast.makeText(MapsActivity.this, "locationListenerGPS: default", Toast.LENGTH_SHORT).show();
                    if(!notTrackingMyLocation){
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                locationListenerNetwork);
                    }

                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void changeView(View view) {

        //change between satellite and map view
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            Log.d("MyMapsApp", "changeView: Changing to satellite view");

        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            Log.d("MyMapsApp", "changeView: Changing to map view");

        }
    }

    public void onSearch(View v) {
        //Retrieve the location in the search box
        String location = locationSearch.getText().toString();

        //Create a list of addresses
        //Address is a set of Strings describing a location
        //Format is in xAL (eXtensible Address Language
        List<Address> addressList = null;
        List<Address> addressListZip = null;


        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMapsApp", "onSearch: location= " + location);
        Log.d("MyMapsApp", "onSearch: provider= " + provider);

        LatLng userLocation = null;
        try {
            //Check the last known location, need to specifically list the provider (network or gps)
            if (locationManager != null) {
                Log.d("MyMapsApp", "onSearch: locationManager is not null");
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","onSearch: using NETWORK_PROVIDER userLocation is "+ myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this,"UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                }
                else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp","onSearch: using GPS_PROVIDER userLocation is "+ myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this,"UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("MyMapsApp","onSearch: myLocation is null from getLastKnownLocation with Network provider");
                }
            } else {
                Log.d("MyMapsApp","onSearch: myLocation is null!");
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp","Exception getLastKnownLocation");
            Toast.makeText(this,"Exception getLastKnownLocation",Toast.LENGTH_SHORT).show();
        }

        //Get the location if it exists
        if(!location.matches("")){
            Log.d("MyMapsApp","onSearch: location field is populated");

            Geocoder geocoder = new Geocoder(this, Locale.US);

            Log.d("MyMapsApp","onSearch: created a new Geocoder");

            try{
                addressList = geocoder.getFromLocationName(location,10000,
                        userLocation.latitude - (5.0/60.0),
                        userLocation.longitude - (5.0/60.0),
                        userLocation.latitude + (5.0/60.0),
                        userLocation.longitude + (5.0/60.0));

                Log.d("MyMapsApp","onSearch: created addressList");

            }catch (IOException e){
                e.printStackTrace();
            }

            if(!addressList.isEmpty()) {
                Log.d("MyMapsApp","addressList size = " + addressList.size());
                for(int i = 0; i < addressList.size();i++){
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());

                    mMap.addMarker(new MarkerOptions().position(latLng).title(i+": "+
                            address.getSubThoroughfare()+" "+address.getThoroughfare()));

                    Log.d("MyMapsApp","onSearch: added Marker");

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
    }

    public void dropMarker(String provider) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocation = locationManager.getLastKnownLocation(provider);
        if(myLocation != null){
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
        }

        LatLng userLocation = null;
        if(myLocation == null){
            Toast.makeText(this,"dropAMarker: my location is null", Toast.LENGTH_SHORT).show();
        }
        else{
            userLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation,MY_LOCATION_ZOOM_FACTOR);

            if(provider == LocationManager.GPS_PROVIDER){
                //add circles for the marker with two outer rings
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.RED));

                Circle ring1 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1.5)
                        .strokeColor(Color.RED)
                        .strokeWidth(1));
                //add outer ring 2
                Circle ring2 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(2)
                        .strokeColor(Color.RED)
                        .strokeWidth(1));
            }
            else{
                //add circles for the marker with two outer rings
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.BLUE));
                //add outer ring 1
                Circle ring1 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1.5)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(1));
                //add outer ring 2
                Circle ring2 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(2)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(1));
            }

            mMap.animateCamera(update);
        }
    }

    //Write method trackMyLocation(View view) -- call getLocation if currently not tracking
    //or turn of tracking if currently enabled (removeUpdates for both listeners

    public void trackMyLocation(View view){
        Log.d("MyMapsApp","tracking now");

        if(notTrackingMyLocation){
            getLocation();
            Toast.makeText(this,"trackMyLocation: tracking is On", Toast.LENGTH_SHORT).show();
            Log.d("MyMapsApp","trackMyLocation is On");
            notTrackingMyLocation = false;
        }else{
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            Toast.makeText(this,"trackMyLocation: tracking is Off", Toast.LENGTH_SHORT).show();
            Log.d("MyMapsApp","trackMyLocation is Off");
            notTrackingMyLocation = true;
        }
    }

    //Write method clearMarkers(View view)
    //Clear all markers from map
    public void clearMarkers(View view){
        trackMarkerDropCounter = 0;
        mMap.clear();
    }
}