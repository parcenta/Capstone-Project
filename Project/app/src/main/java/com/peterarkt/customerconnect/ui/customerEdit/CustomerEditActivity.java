package com.peterarkt.customerconnect.ui.customerEdit;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.databinding.ActivityCustomerEditBinding;
import com.peterarkt.customerconnect.ui.utils.MediaUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public class CustomerEditActivity extends AppCompatActivity implements OnMapReadyCallback {

    // For Intent Variables
    private static String PANEL_MODE = "PANEL_MODE";
    private static String CUSTOMER_ID = "CUSTOMER_ID";

    // Intent Variables
    private String mPanelMode;
    private int mCustomerId;


    // Panel Variables that must persist.
    private boolean customerIsSaving = false;
    private String mPhotoPath = "";
    private double mLatitude = 0.00;
    private double mLongitude = 0.00;

    // For Loaders
    public static final int LOAD_CUSTOMER_LOADER_ID = 7100;
    public static final int SAVE_CUSTOMER_LOADER_ID = 7101;

    // For Camera / Attach pictures
    static final int REQUEST_IMAGE_PHOTO = 2001;

    // For SavedBundleInstance
    public static final String CUSTOMER_IS_SAVING = "CUSTOMER_IS_SAVING";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    public static final String PHOTO_PATH = "PHOTO_PATH";

    // Binding.
    ActivityCustomerEditBinding mBinding;
    CustomerEditViewModel viewModel;

    // For Location
    private final static int LOCATION_PERMISSION_GRANTED_ID = 6001;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    // Map
    private GoogleMap mMap;


    /* -----------------------------------------------------------------
     * Launch Helper
     * -----------------------------------------------------------------*/
    public static void launch(Context context, String panelMode, int customerId) {
        context.startActivity(launchIntent(context, panelMode, customerId));
    }

    private static Intent launchIntent(Context context, String panelMode, int customerId) {
        Class destinationActivity = CustomerEditActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra(PANEL_MODE, panelMode);
        intent.putExtra(CUSTOMER_ID, customerId);
        return intent;
    }


    /* -----------------------------------------------------------------
     * OnCreate
     * -----------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_customer_edit);

        // Get Values from intent
        Intent receivedIntent = getIntent();
        mPanelMode = receivedIntent.hasExtra(PANEL_MODE) ? receivedIntent.getStringExtra(PANEL_MODE) : "";
        mCustomerId = receivedIntent.hasExtra(CUSTOMER_ID) ? receivedIntent.getIntExtra(CUSTOMER_ID, 0) : 0;


        // Set Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.phone_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.phoneLayout.inputCustomerPhoneTypeSpinner.setAdapter(adapter);


        // Set the Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        //
        mBinding.photoLayout.actionTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        // Set Action to the "Save Button".
        mBinding.actionSaveCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerIsSaving = true;
                getSupportLoaderManager().restartLoader(SAVE_CUSTOMER_LOADER_ID, null, saveCustomerLoaderListener);
            }
        });

        // Set Action to the "Delete Customer Photo button"
        mBinding.photoLayout.actionDeleteCustomerPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhotoPath = "";
                refreshCustomerPhotoUI();
            }
        });

        // Set action to get current location.
        mBinding.locationLayout.actionSearchCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
            }
        });

        // ---------------------------------------------------------------------------------
        // If customer was saving (Remembered we recovered the value in onRestoreInstance)
        // ---------------------------------------------------------------------------------
        if (customerIsSaving)
            getSupportLoaderManager().initLoader(SAVE_CUSTOMER_LOADER_ID, null, saveCustomerLoaderListener);

        // Refreshing the Map.
        refreshMap();

        // Refresh the Photo Holder
        refreshCustomerPhotoUI();
    }

    /* -----------------------------------------------------------------------------------------------------------------
    * Map Fragment
    * -----------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.

    }

    private void refreshMap() {
        if (mMap == null) return;

        // Clearing the map.
        mMap.clear();

        // If there is a valid Latitude and Longitude, then show the marker in that position.
        if (mLatitude != 0.00 || mLongitude != 0.00) {
            LatLng coordinates = new LatLng(mLatitude, mLongitude);
            mMap.addMarker(new MarkerOptions().position(coordinates).title("Current Position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
        }
    }

    /* -----------------------------------------------------------------------------------------------------------------
    *   OnSaveInstance/OnRestoreInstance
    ----------------------------------------------------------------------------------------------------------------- */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CUSTOMER_IS_SAVING, customerIsSaving);
        outState.putDouble(LATITUDE, mLatitude);
        outState.putDouble(LONGITUDE, mLongitude);
        outState.putString(PHOTO_PATH, mPhotoPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        customerIsSaving = savedInstanceState.getBoolean(CUSTOMER_IS_SAVING);
        mLatitude = savedInstanceState.getDouble(LATITUDE);
        mLongitude = savedInstanceState.getDouble(LONGITUDE);
        mPhotoPath = savedInstanceState.getString(PHOTO_PATH);
    }

    /* -----------------------------------------------------------------------------------------------------------------
    * Open Camera helper
    ----------------------------------------------------------------------------------------------------------------- */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                // Create the file.
                photoFile = MediaUtils.createImageFile(this);

                // Get the Image path.
                mPhotoPath = photoFile.getAbsolutePath();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getString(R.string.content_authority_for_file_provider),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_PHOTO && resultCode == RESULT_OK) refreshCustomerPhotoUI();
    }

    private void refreshCustomerPhotoUI() {

        // If Photo Path is empty, then show the Attach or Add buttons.
        if (mPhotoPath.isEmpty()) {
            Timber.i("File path is empty...");
            mBinding.photoLayout.validPhotoContainer.setVisibility(View.GONE);
            mBinding.photoLayout.addOrAttachPhotoContainer.setVisibility(View.VISIBLE);
            return;
        }

        // If Photo Path is valid...
        Timber.i("File path:" + mPhotoPath);
        mBinding.photoLayout.addOrAttachPhotoContainer.setVisibility(View.GONE);
        mBinding.photoLayout.validPhotoContainer.setVisibility(View.VISIBLE);
        Picasso.with(this)
                .load("file://"+mPhotoPath)
                .error(R.drawable.ic_material_error_gray)
                .fit()
                .into(mBinding.photoLayout.inputCustomerPhotoImageView);
    }

    /* -----------------------------------------------------------------------------------------------------------------
    * Location Methods
    * -----------------------------------------------------------------------------------------------------------------*/

    private void getCurrentLocation() {
        // First check if LOCATION permission is enabled. If not then ask for it.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_GRANTED_ID);
            return;
        }

        // Show Progress bar.
        mBinding.locationLayout.actionSearchCurrentLocation.setVisibility(View.GONE);
        mBinding.locationLayout.currentLocationProgressBar.setVisibility(View.VISIBLE);

        // Create the Client.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setting the LocationRequest
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); // Search location each 5 seconds.
        mLocationRequest.setFastestInterval(2500); // Fastest interval set in 2.5 seg
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//getLocationPriority(batteryLevel)

        // Setting Location Callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                if (location != null){

                    // Stop the Location updates, beacuse we already have a valid location.
                    stopLocationUpdates();

                    // Set the latitude and longitude from the valid location.
                    mLatitude   = location.getLatitude();
                    mLongitude  = location.getLongitude();
                    Timber.d("Latitude: " + mLatitude + ", Longitude:" + mLongitude);

                    // Refreshing the map with the given coordinates
                    refreshMap();
                }
            }
        };

        // Start to request location updates
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());

    }

    private void stopLocationUpdates(){
        try {
            if (mFusedLocationClient!= null) mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Source: https://developer.android.com/training/permissions/requesting.html#java
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_GRANTED_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,R.string.permission_granted_message,Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,R.string.permission_denied_message,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Setop the location updates if the activity is paused.
        stopLocationUpdates();
    }

    /* -----------------------------------------------------------------------------------------------------------------
             * Save Customer Loader
             -------------------------------------------------------------------------------------------------------------------*/
    private LoaderManager.LoaderCallbacks<String> saveCustomerLoaderListener
            = new LoaderManager.LoaderCallbacks<String>(){

        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<String>(CustomerEditActivity.this) {

                String cachedErrorMessage;

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if(cachedErrorMessage!=null)
                        deliverResult(cachedErrorMessage);
                    else
                        forceLoad();
                }

                @Override
                public String loadInBackground() {

                    if(mBinding == null) return "An error has ocurred. Please try again.";

                    // Extracting data from the views into one single object.
                    CustomerEditViewModel newCustomer     = new CustomerEditViewModel();
                    newCustomer.customerId                  = 0; // If the CustomerId is zero, internally will create an ID in INS mode.
                    newCustomer.customerName                = mBinding.basicInfoLayout.inputCustomerName.getText().toString().trim();
                    newCustomer.customerPhoneNumber         = mBinding.phoneLayout.inputCustomerPhoneNumber.getText().toString().trim();
                    newCustomer.customerEmail               = mBinding.emailLayout.inputCustomerEmail.getText().toString().trim();
                    newCustomer.customerPhoneType           = String.valueOf(mBinding.phoneLayout.inputCustomerPhoneTypeSpinner.getSelectedItemId());
                    newCustomer.customerAddressStreet       = mBinding.locationLayout.inputCustomerAddressStreet.getText().toString();
                    newCustomer.customerAddressCity         = mBinding.locationLayout.inputCustomerAddressCity.getText().toString();
                    newCustomer.customerAdressCountry       = mBinding.locationLayout.inputCustomerAddressCountry.getText().toString();
                    newCustomer.customerAddressLatitude     = mLatitude;
                    newCustomer.customerAddressLatitude     = mLongitude;
                    newCustomer.customerPhotoPath           = mPhotoPath;

                    return CustomerEditHelper.createCustomer(CustomerEditActivity.this,newCustomer);
                }

                @Override
                public void deliverResult(String errorMessage) {
                    cachedErrorMessage = errorMessage;
                    super.deliverResult(cachedErrorMessage);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String errorMessage) {
            // Turn off flag.
            customerIsSaving = false;


            // if everything is fine...
            if(errorMessage != null && errorMessage.isEmpty()){
                Toast.makeText(CustomerEditActivity.this,getString(R.string.customer_created_successfully),Toast.LENGTH_SHORT).show();
                finish();
            } //... if something fails...
            else
                Toast.makeText(CustomerEditActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
        }
    };
}
