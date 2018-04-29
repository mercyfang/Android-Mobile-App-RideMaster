package edu.duke.compsci290.ridermaster.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.google.android.gms.location.places.AutocompletePrediction;
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
import Utilities.UtilityFunctions;
import edu.duke.compsci290.ridermaster.R;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener{

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static final String TAG = "MapActivity";


    /**
     * Permissions to display the Map
     */
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;                //Until it checks all the permission are granted, this doesn't change to true

    /**
     * Default Map Parameters
     */
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40,-168), new LatLng(71, 136));

    /**
     * Allows to pick locations nearby your current location
     */
    private static final int PLACE_PICKER_REQUEST = 1;


    /**
     * Map Objects
     */
    private GoogleMap mMap;                                                //Actual Map object
    private FusedLocationProviderClient mFusedLocationProviderClient;      //combines GPS location and network location to get accurate location data
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapterStarting;    //Adapter for the list that automatically provides suggestions (For the Starting location search bar)
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapterDestination; //Adapter for the list that automatically provides suggestions (For the Destination search bar)
    private GoogleApiClient mGoogleApiClient;                              //Provides to access to Google's APIs
    private PlaceInfo mPlace;                                              //Provides data for a given Place object
    private Marker mMarker;                                                //Marker object
    protected GeoDataClient mGeoDataClientStarting;                        //Object that allows the map to find a place (for starting search bar)
    protected GeoDataClient mGeoDataClientDestination;                     //Object that allows the map to find a place (for destination search bar)
    private PlaceDetectionClient mPlaceDetectionClientStarting;            //Object that allows the map to find a place (for starting search bar)
    private PlaceDetectionClient mPlaceDetectionClientDestination;         //Object that allows the map to find a place (for destination search bar)

    /**
     * Variables
     */
    private double myStartingLng;                                          //Starting location longitude
    private double myStartingLat;                                          //Starting location latitude
    private double myDestLng;                                              //Destination location longitude
    private double myDestLat;                                              //Destination location latitude
    private double myDeviceLng;                                            //Device's location longitude
    private double myDeviceLat;                                            //Device's location latitude
    private double diffLat;                                                //Difference between the starting and destination latitude
    private double diffLng;                                                //Difference between the starting and destination longitude
    private double mMidPointLat;                                           //Mid Point latitude between starting and destination latitude
    private double mMidPointLng;                                           //Mid Point longitude between starting and destination longitude
    private LatLng pickUpLatLng;                                           //Pick up location latlng object @Todo Remove

    /**
     * UI Elements
     */
    private AutoCompleteTextView mSearchTextStarting;                      //View that holds the text being typed for the starting location search bar
    private AutoCompleteTextView mSearchTextDestination;                   //View that holds the text being typed for the destination location search bar
    private ImageView mGps, mInfo, mPlacePicker;                           //Views that hold the icons @Todo Remove
    private Button mConfirmButton;                                         //Button that confirms user's starting and destination location

    /**
     * When the activity is first created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //loads the pick up point from the previous run @Todo Remove
        loadData();

        //Attaches the xml for the map
        setContentView(R.layout.activity_map);

        //Attaches the objects to their associated UI elements
        mSearchTextStarting = findViewById(R.id.input_search);
        mSearchTextDestination = findViewById(R.id.input_search_destination);
        mGps = findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.place_info);
        mPlacePicker = findViewById(R.id.place_picker);
        mConfirmButton = findViewById(R.id.confirm_button);

        /**
         * After the Confirm Button is pressed, Ride Request Activity is launched
         */
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
                Intent intent = new Intent(MapActivity.this, RideRequestActivity2.class);
                startActivity(intent);
            }
        });
        Log.d(TAG, "onCreate:InputType: " + mSearchTextStarting.getInputType());  //@Todo add Logger

        //Doesn't allow anything to be typed in the Search Destination Box when it's initialized
        mSearchTextDestination.setInputType(0);

        getLocationPermissionsAndInitMap();
    }

    /**
     * Map makes an internal call to this function
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: map is ready"); //@Todo Logger

        //if permission is granted then map is able to display user's current location
        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            //geoLocatePickUp();

            //If permission to get user's current location is not in the manifest then location is not set
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }


            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            addStartingDestinationLocationToMap();


        }
    }

    /**
     * Takes in the user typed starting and destination location
     * Adds marker to those locations
     * Resizes the Map View to display both locations
     */
    private void addStartingDestinationLocationToMap(){
        Log.d(TAG, "addStartingDestinationLocationToMap: initializing"); //@Todo Logger

        //instantiates google api client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mSearchTextStarting.setOnItemClickListener(mAutocompleteClickListener);
//      mSearchTextDestination.setOnItemClickListener(mAutocompleteClickListenerDestination);

        mGeoDataClientStarting = Places.getGeoDataClient(this);
        mGeoDataClientDestination = Places.getGeoDataClient(this);

        mPlaceDetectionClientStarting = Places.getPlaceDetectionClient(this);
        mPlaceDetectionClientDestination = Places.getPlaceDetectionClient(this);

        mPlaceAutocompleteAdapterStarting = new PlaceAutocompleteAdapter(this, mGeoDataClientStarting, LAT_LNG_BOUNDS, null);
        mPlaceAutocompleteAdapterDestination = new PlaceAutocompleteAdapter(this, mGeoDataClientStarting, LAT_LNG_BOUNDS, null);

        mSearchTextStarting.setAdapter(mPlaceAutocompleteAdapterStarting);
        mSearchTextStarting.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /**
             * When the Enter is clicked for the starting location search
             * @param v
             * @param actionId
             * @param keyEvent
             * @return
             */
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    moveMapToStartingLocation();


                    //Makes the Search Text for destination editable
                    mSearchTextDestination.setInputType(589825);
                    mSearchTextDestination.setText("");
                    mSearchTextDestination.setTextColor(getResources().getColor(R.color.black));
                    //Toast.makeText(MapActivity.this, String.valueOf(myStartingLat) + "," + String.valueOf(myStartingLng), Toast.LENGTH_SHORT).show();
                    mConfirmButton.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        mSearchTextDestination.setAdapter(mPlaceAutocompleteAdapterDestination);
        mSearchTextDestination.setOnClickListener(new View.OnClickListener() {
            /**
             * Checks if the user has entered the first destination properly and if not then tells them to do the step that they are missing
             * @param v
             */
            @Override
            public void onClick(View v) {
                if(mSearchTextDestination.getInputType() == 0 && !mSearchTextStarting.getText().toString().trim().equals("")){
                    Log.d(TAG, String.format("SearchText.text: -_-_-%s-_-_-", mSearchTextStarting.getText().toString().trim())); //@Todo Logger
                    mSearchTextDestination.setText("Press Enter for the Starting Location");
                    mSearchTextDestination.setTextColor(getResources().getColor(R.color.red));
                }
                if(mSearchTextDestination.getInputType() == 0 && mSearchTextStarting.getText().toString().trim().equals("")){
                    //Log.d(TAG, String.format("SearchText.text: -_-_-%s-_-_-", mSearchTextStarting.getText().toString().trim()));
                    mSearchTextDestination.setText("Input Starting Location and press Enter");
                    mSearchTextDestination.setTextColor(getResources().getColor(R.color.red));
                }
            }
        });

        mSearchTextDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /**
             * After user has press Enter on the second search bar, then both the maps resizes to include both location
             * @param v
             * @param actionId
             * @param event
             * @return
             */
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN ||
                        event.getAction() == KeyEvent.KEYCODE_ENTER){

                    moveMapToDestinationLocation();
                    mMidPointLat = (myDestLat + myStartingLat)/2;
                    mMidPointLng = (myDestLng + myStartingLng)/2;
//                    Toast.makeText(MapActivity.this, String.valueOf(diffLat) + "," + String.valueOf(diffLng), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MapActivity.this, String.valueOf(mMidPointLat) + "," + String.valueOf(mMidPointLng), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        //@ToDo remove GPS
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: click gps icon");
                getDeviceLocation();
            }
        });

        //@ToDo remove mInfo
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

        //@ToDo remove place picker
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

        //@Todo remove
        geoLocatePickUp();
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


    /**
     * Gets the user's input starting location text
     * Converts the address into latitude and longitude coordinates
     * sets the  global variables to the retrieved latitue and longitude coordinates
     *
     */
    private void moveMapToStartingLocation(){
        Log.d(TAG, "moveMapToStartingLocation: geolocating"); //@Todo Logger

        String searchString = mSearchTextStarting.getText().toString();

        //geocoder converts addresses into latitude and longitude coordinates
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);

        } catch (IOException e){
            Log.e(TAG, "moveMapToStartingLocation: IOException" + e.getMessage());
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "moveMapToStartingLocation: found a location: " + address.toString()); //@Todo Logger

            myStartingLat = address.getLatitude();
            myStartingLng = address.getLongitude();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    /**
     * First gets the user's starting location
     * Gets the user's destination location
     * resizes the view so that both locations are visible and adds two markers
     */
    private void moveMapToDestinationLocation(){

        Log.d(TAG, "moveMapToStartingLocation: geolocating");

        String searchString = mSearchTextDestination.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);

        } catch (IOException e){
            Log.e(TAG, "moveMapToStartingLocation: IOException" + e.getMessage());
        }
        Address address = null;
        if(list.size() > 0){
            address = list.get(0);

            Log.d(TAG, "moveMapToStartingLocation: found a location: " + address.toString()); //@Todo Logger

            myDestLat = address.getLatitude();
            myDestLng = address.getLongitude();
//            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
//                    address.getAddressLine(0));
        }

        String searchString2 = mSearchTextStarting.getText().toString();
        Geocoder geocoder2 = new Geocoder(MapActivity.this);
        List<Address> list2 = new ArrayList<>();
        try{
            list2 = geocoder2.getFromLocationName(searchString2, 1);
        } catch (IOException e) {
            Log.e(TAG, "moveMapToDestinationLocation: IOException" + e.getMessage()); //@Todo Logger

        }
        Address address2 = null;
        if (list2.size() > 0){
            address2= list2.get(0);
            myStartingLat = address2.getLatitude();
            myStartingLng = address2.getLongitude();
        }

        mMidPointLat = (myDestLat + myStartingLat)/2;
        mMidPointLng = (myDestLng + myStartingLng)/2;

        LatLng latlng1 = new LatLng(myStartingLat, myStartingLng);
        LatLng latlng2 = new LatLng(myDestLat, myDestLng);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(zoomBounds(latlng1, latlng2) ,100));
        MarkerOptions options1 = new MarkerOptions()
                .position(latlng2);
        mMap.addMarker(options1);
        hideSoftKeyboard();
    }

    //@Todo Remove
    private void geoLocatePickUp(){
        LatLng latlng1 = new LatLng(pickUpLatLng.latitude, pickUpLatLng.longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(zoomBounds(pickUpLatLng, pickUpLatLng) ,100));
        MarkerOptions options2 = new MarkerOptions()
                .position(pickUpLatLng);
        mMap.addMarker(options2);
        hideSoftKeyboard();
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devicescurrent location"); //@Todo Logger

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        /**
         * When the permission is granted, it finds the user's device location
         * Sets the global variables of the Lat and Lng
         * Resizes the map to display the device location
         */
        try {
            if (mLocationPermissionsGranted) {
                com.google.android.gms.tasks.Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location"); //@Todo Logger
                            Location currentLocation = (Location) task.getResult();
                            myDeviceLat = currentLocation.getLatitude();
                            myDeviceLng = currentLocation.getLongitude();
                            LatLng myLatLng = new LatLng(myDeviceLat, myDeviceLng);

                            //resizes map's location to encompass a latlng
                            moveCamera(myLatLng, DEFAULT_ZOOM, "My Location");
                            

                        } else {
                            Log.d(TAG, "onComplete: current location is null"); //@Todo Logger
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show(); //@Todo Toaster
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }

    }

    /**
     * Moves the camera to the place near you
     * @param latLng
     * @param zoom
     * @param placeInfo
     */
    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude); //@Todo Logger
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

    /**
     * Resizes the map to a given Zoom Bound, given the coordinates of a place
     * @param latLng
     * @param zoom
     * @param title
     */
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //Does not place marker on the user's device location, but only the marker if the user's device location is not the searched location
        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();
    }

    /**
     * Checks the permissions in order to use the map
     */
    private void getLocationPermissionsAndInitMap() {

        Log.d(TAG, "getLocationPermissionsAndInitMap: getting location permissions"); //@ToDo Add Logger

        //Array of permissions to check
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};


        /**
         * If all permissions are granted initializes map, else requests the permissions
         */
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

    /**
     * When the permission request results are recevied, the permission boolean is set to true if the permission is granted and initializes map
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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
                            Log.d(TAG, "onRequestPermissionsResult: permission failed"); //@Todo Logger
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted"); //@Todo Logger
                    mLocationPermissionsGranted = true;

                    initMap();
                }
            }
        }
    }

    /**
     * Initializes Map fragment from the XML
     */
    private void initMap() {
        Log.d(TAG, "initMap: initializing Map"); //@Todo Logger

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //MapsAsync initializes the map system and view
        mapFragment.getMapAsync(MapActivity.this);
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show(); //@Todo Toaster
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapterStarting.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);

            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };
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

    /**
     * When the text is entered, a request goes out to find possible location
     * This CallBack occurs once the results are obtained
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {

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

            places.release(); //to avoid memory leak
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
//            moveCamera(new LatLng(mMidPointLat, mMidPointLng), DEFAULT_ZOOM, mPlace);
//            hideSoftKeyboard();
            places.release();
        }
    };

    /**
     * Creates a bound object for the two Lat Lng points that are given
     * @param sp
     * @param ep
     * @return
     */
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

    /**
     * saves the information that the user's inputted for the starting and destination
     * this info is shared with the ride request activity
     */
    public void saveInfo(){
        SharedPreferences sharedPref = getSharedPreferences("UserPathInfo", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor = UtilityFunctions.putDouble(editor, "Starting Location Latitude", myStartingLat);
        editor = UtilityFunctions.putDouble(editor, "Starting Location Longitude", myStartingLng);
        editor.putString("Starting Location Text", mSearchTextStarting.getText().toString());
        editor = UtilityFunctions.putDouble(editor, "Destination Location Latitude", myDestLat);
        editor = UtilityFunctions.putDouble(editor, "Destination Location Longitude", myDestLng);
        editor.putString("Destination Location Text", mSearchTextDestination.getText().toString());
        editor.commit();

        Log.d(TAG, "saveInfo: " + myStartingLat + myStartingLng + mSearchTextStarting.getText() + myDestLat + myDestLng + mSearchTextDestination.getText());
        Log.d(TAG, "saveInfo: "+ sharedPref.getAll().toString());

    }

    //@todo remove
    public void loadData(){

        SharedPreferences sharedPref = getSharedPreferences("PickUpPointInfo", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        double myStartingLat = UtilityFunctions.getDouble(sharedPref, "PickUp Location Latitude", 0);
        double myStartingLng = UtilityFunctions.getDouble(sharedPref, "PickUp Location Longitude", 0);
        pickUpLatLng =  new LatLng(myStartingLat, myStartingLng);


        Log.d(TAG, "loadData: " + myStartingLat + ":" + myStartingLng);

    }

}
