package edu.duke.compsci290.ridermaster.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.duke.compsci290.ridermaster.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener{

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40,-168), new LatLng(71, 136));
    private static final int PLACE_PICKER_REQUEST = 1;


    //vars
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapterDestination;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    double myStartingLng;
    double myStartingLat;
    double myDestLng;
    double myDestLat;
    double myDeviceLng;
    double myDeviceLat;
    double diffLat;
    double diffLng;
    double mLat;
    double mLng;
    float mFLat = (float) mLat;
    float mFLng = (float) mLng;



    protected GeoDataClient mGeoDataClient;
    protected GeoDataClient mGeoDataClientDestination;

    PlaceDetectionClient mPlaceDetectionClient;
    PlaceDetectionClient mPlaceDetectionClientDestination;


    //widgets
    private AutoCompleteTextView mSearchText;
    private AutoCompleteTextView mSearchTextDestination;
    private ImageView mGps, mInfo, mPlacePicker;

    private Boolean mLocationPermissionsGranted = false;
    
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mSearchTextDestination = (AutoCompleteTextView) findViewById(R.id.input_search_destination);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mInfo = (ImageView) findViewById(R.id.place_info);
        mPlacePicker = (ImageView) findViewById(R.id.place_picker);


        Log.d(TAG, "onCreate: boop");

        getLocationPermissions();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: map is ready");

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

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
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();

        }
    }

    private void init(){
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

//        mSearchText.setOnItemClickListener(mAutocompleteClickListener);
//        mSearchTextDestination.setOnItemClickListener(mAutocompleteClickListenerDestination);

        mGeoDataClient = Places.getGeoDataClient(this);
        mGeoDataClientDestination = Places.getGeoDataClient(this);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mPlaceDetectionClientDestination = Places.getPlaceDetectionClient(this);



        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,mGeoDataClient, LAT_LNG_BOUNDS, null);
        mPlaceAutocompleteAdapterDestination = new PlaceAutocompleteAdapter(this,mGeoDataClient, LAT_LNG_BOUNDS, null);

       mSearchText.setAdapter(mPlaceAutocompleteAdapter);


        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    geoLocate();
                    Toast.makeText(MapActivity.this, String.valueOf(myStartingLat) + "," + String.valueOf(myStartingLng), Toast.LENGTH_SHORT).show();


                }

                return false;
            }
        });

        mSearchTextDestination.setAdapter(mPlaceAutocompleteAdapterDestination);
        mSearchTextDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN ||
                        event.getAction() == KeyEvent.KEYCODE_ENTER){

                    geoLocateSearchBarTwo();
                    diffLat = myDestLat - myStartingLat;
                    diffLng = myDestLng - myDestLng;
                    mLat = (myDestLat + myStartingLat)/2;
                    mLng = (myDestLng + myStartingLng)/2;


//                    Toast.makeText(MapActivity.this, String.valueOf(diffLat) + "," + String.valueOf(diffLng), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MapActivity.this, String.valueOf(mLat) + "," + String.valueOf(mLng), Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
        
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: click gps icon");
                getDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }
                    else {
                        Log.d(TAG, "onClick: place info" + mPlace.toString());
                        mMarker.showInfoWindow();
                    }

                } catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException" + e.getMessage());

                }

            }
        });

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException: "
                    + e.getMessage());
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException"
                    + e.getMessage());
                }

            }
        });
        hideSoftKeyboard();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient
                        , place.getId());

                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();


            }
        }
    }



    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);

        } catch (IOException e){
            Log.e(TAG, "geoLocate: IOException" + e.getMessage());
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            myStartingLat = address.getLatitude();
            myStartingLng = address.getLongitude();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));


        }
    }

    private void geoLocateSearchBarTwo(){

        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchTextDestination.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);

        } catch (IOException e){
            Log.e(TAG, "geoLocate: IOException" + e.getMessage());
        }
        Address address = null;
        if(list.size() > 0){
            address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            myDestLat = address.getLatitude();
            myDestLng = address.getLongitude();



            
//            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
//                    address.getAddressLine(0));




        }

        String searchString2 = mSearchText.getText().toString();
        Geocoder geocoder2 = new Geocoder(MapActivity.this);
        List<Address> list2 = new ArrayList<>();
        try{
            list2 = geocoder2.getFromLocationName(searchString2, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocateSearchBarTwo: IOException" + e.getMessage());

        }
        Address address2 = null;
        if (list2.size() > 0){
            address2= list2.get(0);
            myStartingLat = address2.getLatitude();
            myStartingLng = address2.getLongitude();
        }

        mLat = (myDestLat + myStartingLat)/2;
        mLng = (myDestLng + myStartingLng)/2;

        LatLng latlng1 = new LatLng(myStartingLat, myStartingLng);
        LatLng latlng2 = new LatLng(myDestLat, myDestLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(zoomBounds(latlng1, latlng2) ,100));
        hideSoftKeyboard();

    }




    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devicescurrent location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {
                com.google.android.gms.tasks.Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();
                            myDeviceLat = currentLocation.getLatitude();
                            myDeviceLng = currentLocation.getLongitude();
                            LatLng myLatLng = new LatLng(myDeviceLat, myDeviceLng);

                            //Toast.makeText(MapActivity.this, String.valueOf(myDeviceLat) + "," + String.valueOf(myDeviceLng), Toast.LENGTH_SHORT).show();

                            moveCamera(myLatLng,
                                    DEFAULT_ZOOM,
                                    "My Location");
                            

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }


    }
    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        
        mMap.clear();


        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
        if (placeInfo != null){
            try{

                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsite() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);

                mMarker = mMap.addMarker(options);

                
            } catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointException" + e.getMessage() );
            }
        } else {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }


        hideSoftKeyboard();

    }


    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);

        //}

        hideSoftKeyboard();

    }

    private void getLocationPermissions() {
        Log.d(TAG, "getLocationPermissions: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;

                    initMap();
                }
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Log.d(TAG, "initMap: initializing Map");
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();

        mapFragment.getMapAsync(MapActivity.this);


    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

//    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            hideSoftKeyboard();
//
//            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
//            final String placeId = item.getPlaceId();
//
//            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
//
//            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
//        }
//    };
//
//    private AdapterView.OnItemClickListener mAutocompleteClickListenerDestination = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            hideSoftKeyboard();
//
//            final AutocompletePrediction item = mPlaceAutocompleteAdapterDestination.getItem(position);
//            final String placeId = item.getPlaceId();
//
//            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
//
//            placeResult.setResultCallback(mUpdatePlaceDetailsCallbackDestination);
//        }
//    };
    
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            hideSoftKeyboard();
            if (!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: place query did not complete successfully: " + places.getStatus().toString());
                places.release(); //to prevent memory leak
                return;
            }
            final Place place = places.get(0);

            try {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
                //mPlace.setAttribution(place.getAttributions().toString());
                mPlace.setId(place.getId().toString());
                mPlace.setLatlng(place.getLatLng());
                mPlace.setRating(place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setWebsite(place.getWebsiteUri());


                Log.d(TAG, "onResult: place: " + mPlace.toString());

            } catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException "
                        + e.getMessage());
            }
            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude),DEFAULT_ZOOM, mPlace);
            hideSoftKeyboard();

            places.release();


            
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallbackDestination = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            hideSoftKeyboard();
            if (!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: place query did not complete successfully: " + places.getStatus().toString());
                places.release(); //to prevent memory leak
                return;



            }
            final Place place = places.get(0);


            try {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
                //mPlace.setAttribution(place.getAttributions().toString());
                mPlace.setId(place.getId().toString());
                mPlace.setLatlng(place.getLatLng());
                mPlace.setRating(place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setWebsite(place.getWebsiteUri());


                Log.d(TAG, "onResult: place: " + mPlace.toString());

            } catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException "
                        + e.getMessage());
            }




            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude),DEFAULT_ZOOM, mPlace);

//            moveCamera(new LatLng(mLat, mLng), DEFAULT_ZOOM, mPlace);
//            hideSoftKeyboard();

            places.release();



        }
    };

    public LatLngBounds zoomBounds(LatLng sp, LatLng ep){
        //starting point - sp
        //ending point - ep

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(sp);
        boundsBuilder.include(ep);
// pan to see all markers on map:
        LatLngBounds bounds = boundsBuilder.build();
        return bounds;



    }



}
