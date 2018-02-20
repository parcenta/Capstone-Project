package com.peterarkt.customerconnect.ui.customerEdit;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
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
import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.databinding.ActivityCustomerEditBinding;
import com.peterarkt.customerconnect.ui.utils.Constants;
import com.peterarkt.customerconnect.ui.utils.MediaUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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

    // For Loaders
    public static final int LOAD_CUSTOMER_LOADER_ID = 7100;
    public static final int SAVE_CUSTOMER_LOADER_ID = 7101;

    // For Camera / Attach pictures
    private final static int READ_EXTERNAL_STORAGE_PERMISSION_GRANTED_ID = 6002;
    private final static int WRITE_STORAGE_AND_CAMERA_PERMISSION_GRANTED_ID = 6003;
    static final int REQUEST_IMAGE_PHOTO_FROM_CAMERA = 2001;
    static final int REQUEST_IMAGE_PHOTO_FROM_GALLERY = 2002;

    // For SavedBundleInstance
    public static final String CUSTOMER_IS_SAVING = "CUSTOMER_IS_SAVING";
    public static final String PANEL_VIEW_MODEL = "PANEL_VIEW_MODEL";

    // Binding.
    ActivityCustomerEditBinding mBinding;

    // Model that will help us to preserve the screen rotation.
    private CustomerEditViewModel mViewModel;

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

        Timber.i("OnCreate");

        // Binding the view.
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_customer_edit);

        // Init the ViewModel. If there is a ViewModel in the saved instance it will be recovered
        // after in the onRestoreInstance.
        mViewModel = new CustomerEditViewModel();

        // Get Values from intent
        Intent receivedIntent = getIntent();
        mPanelMode  = receivedIntent.hasExtra(PANEL_MODE) ? receivedIntent.getStringExtra(PANEL_MODE) : "";
        mCustomerId = receivedIntent.hasExtra(CUSTOMER_ID) ? receivedIntent.getIntExtra(CUSTOMER_ID, 0) : 0;

        // Set action bar, onclick listeners, text changes listener, etc.
        setupUI();

        // ---------------------------------------------------------------------------------
        // If customer was saving (Remembered we recovered the value in onRestoreInstance)
        // ---------------------------------------------------------------------------------
        if (customerIsSaving)
            getSupportLoaderManager().initLoader(SAVE_CUSTOMER_LOADER_ID, null, saveCustomerLoaderListener);

        // ---------------------------------------------------------------------------------
        // If it is UPD mode. Then initLoader to load the customer info.
        // ---------------------------------------------------------------------------------
        if(mPanelMode.equals(Constants.UPDATE_MODE))
            getSupportLoaderManager().initLoader(LOAD_CUSTOMER_LOADER_ID,null,loadCustomerLoaderListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.i("OnResume");
        // Like the OnRestoreInstance is called before onResume (after OnStart) and also the OnRestoreInstance
        // recover the view model, then in this point we can use it and restore the inputs, etc.
        // NOTE: For UPD Mode, we do this on onLoadFinished of its respective loader.
        // Source: https://developer.android.com/guide/components/activities/activity-lifecycle.html
        if(mPanelMode.equals(Constants.INSERT_MODE))
            setupUIFromViewModel();
    }

    private void setupUI(){

        // Set Toolbar
        mBinding.toolbar.setTitle(mPanelMode.equalsIgnoreCase(Constants.UPDATE_MODE) ? getString(R.string.edit_customer) : getString(R.string.new_customer));
        mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Set changes listeners (text changes, etc) to the inputs in order to mantain
        // the viewModel variable as updated as posible.
        setChangeListenersForViewModel();


        // Set Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.phone_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.phoneLayout.inputCustomerPhoneTypeSpinner.setAdapter(adapter);

        // Set the Map Fragment (Loading the Map fragment this way, avoid that it delays the first time that it opens)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            FragmentManager fm = getSupportFragmentManager();
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.map_fragment, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(this);

        // Take photo action.
        mBinding.photoLayout.actionTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });
        mBinding.photoLayout.actionAttachPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaUtils.launchAttachImageForGallery(CustomerEditActivity.this,READ_EXTERNAL_STORAGE_PERMISSION_GRANTED_ID,REQUEST_IMAGE_PHOTO_FROM_GALLERY);
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
                mViewModel.customerPhotoPath = "";
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
    }

    /* -----------------------------------------------------------------------------------------------------------------
    * Map Fragment
    * -----------------------------------------------------------------------------------------------------------------*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        refreshMap();
    }

    private void refreshMap() {
        if (mMap == null || mViewModel == null) return;

        // Clearing the map.
        mMap.clear();

        // If there is a valid Latitude and Longitude, then show the marker in that position.
        if (mViewModel.customerAddressLatitude != 0.00 || mViewModel.customerAddressLongitude != 0.00) {
            LatLng coordinates = new LatLng(mViewModel.customerAddressLatitude, mViewModel.customerAddressLongitude);
            mMap.addMarker(new MarkerOptions().position(coordinates).title("Current Position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));
        }
    }

    /* -----------------------------------------------------------------------------------------------------------------
    *   OnSaveInstance/OnRestoreInstance
    ----------------------------------------------------------------------------------------------------------------- */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Timber.i("OnSaveInstanceState");
        outState.putBoolean(CUSTOMER_IS_SAVING, customerIsSaving);
        outState.putParcelable(PANEL_VIEW_MODEL, mViewModel);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Timber.i("OnRestoreInstance");
        customerIsSaving    = savedInstanceState.getBoolean(CUSTOMER_IS_SAVING);
        mViewModel          = savedInstanceState.getParcelable(PANEL_VIEW_MODEL);
    }

    /* -----------------------------------------------------------------------------------------------------------------
    * Open Camera helper
    ----------------------------------------------------------------------------------------------------------------- */
    private void openCamera() {

        // First check permission for camera and write external storage.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, WRITE_STORAGE_AND_CAMERA_PERMISSION_GRANTED_ID);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                // Create the file.
                photoFile = MediaUtils.createImageFile(this);

                // Get the Image path.
                mViewModel.customerPhotoPath = photoFile.getAbsolutePath();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getString(R.string.content_authority_for_file_provider),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_PHOTO_FROM_CAMERA);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_PHOTO_FROM_CAMERA && resultCode == RESULT_OK) refreshCustomerPhotoUI();
        if (requestCode == REQUEST_IMAGE_PHOTO_FROM_GALLERY && resultCode == RESULT_OK) {
            mViewModel.customerPhotoPath = MediaUtils.getAttachedImagePath(this,data);
            Timber.i("Attached photo path: " + mViewModel.customerPhotoPath);
            refreshCustomerPhotoUI();
        }
    }


    /* -----------------------------------------------------------------------------------------------------------------
    * UI Refreshers
    ----------------------------------------------------------------------------------------------------------------- */
    private void setupUIFromViewModel(){
        if(mViewModel == null) return;

        mBinding.basicInfoLayout.inputCustomerName.setText(mViewModel.customerName);
        mBinding.emailLayout.inputCustomerEmail.setText(mViewModel.customerEmail);
        mBinding.phoneLayout.inputCustomerPhoneNumber.setText(mViewModel.customerPhoneNumber);
        mBinding.locationLayout.inputCustomerAddressStreet.setText(mViewModel.customerAddressStreet);
        mBinding.locationLayout.inputCustomerAddressCity.setText(mViewModel.customerAddressCity);
        mBinding.locationLayout.inputCustomerAddressCountry.setText(mViewModel.customerAdressCountry);

        int selectedPhoneTypePosition = 0;
        try {
            selectedPhoneTypePosition = Integer.parseInt(mViewModel.customerPhoneType);
        }catch (Exception e){
            e.printStackTrace();
        }
        mBinding.phoneLayout.inputCustomerPhoneTypeSpinner.setSelection(selectedPhoneTypePosition);

        // Refreshing the Map.
        refreshMap();

        // Refresh the Photo Holder
        refreshCustomerPhotoUI();
    }

    private void refreshCustomerPhotoUI() {

        if(mViewModel == null) return;

        // If Photo Path is empty, then show the Attach or Add buttons.
        if (mViewModel.customerPhotoPath.isEmpty()) {
            Timber.i("File path is empty...");
            mBinding.photoLayout.validPhotoContainer.setVisibility(View.GONE);
            mBinding.photoLayout.addOrAttachPhotoContainer.setVisibility(View.VISIBLE);
            return;
        }

        // If Photo Path is valid...
        Timber.i("File path:" + mViewModel.customerPhotoPath);
        mBinding.photoLayout.addOrAttachPhotoContainer.setVisibility(View.GONE);
        mBinding.photoLayout.validPhotoContainer.setVisibility(View.VISIBLE);

        // Set Picasso. With a listener to get a message when image failed to load.
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.listener(new Picasso.Listener()
        {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
            {
                exception.printStackTrace();
            }
        });
        builder.build()
                .load("file://" + mViewModel.customerPhotoPath)
                .error(R.drawable.ic_material_error_gray)
                .fit()
                .centerInside()
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
                    mViewModel.customerAddressLatitude   = location.getLatitude();
                    mViewModel.customerAddressLongitude  = location.getLongitude();
                    Timber.d("Latitude: " + mViewModel.customerAddressLatitude + ", Longitude:" + mViewModel.customerAddressLongitude);

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

            // Hide Progress bar.
            mBinding.locationLayout.currentLocationProgressBar.setVisibility(View.GONE);
            mBinding.locationLayout.actionSearchCurrentLocation.setVisibility(View.VISIBLE);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------------------------------
    //  onRequestPermissionsResult
    // Source: https://developer.android.com/training/permissions/requesting.html#java
    // --------------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_GRANTED_ID
                || requestCode == READ_EXTERNAL_STORAGE_PERMISSION_GRANTED_ID
                || requestCode == WRITE_STORAGE_AND_CAMERA_PERMISSION_GRANTED_ID){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,R.string.permission_granted_message,Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this,R.string.permission_denied_message,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Setop the location updates if the activity is paused.
        stopLocationUpdates();
    }

     /* -----------------------------------------------------------------------------------------------------------------
     * Changing listeners for ViewModel
     * To mantain the viewMode as updated as possible.
     -------------------------------------------------------------------------------------------------------------------*/
    private void setChangeListenersForViewModel(){

        // Customer name
        RxTextView.textChanges(mBinding.basicInfoLayout.inputCustomerName)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        Timber.i("CustomerName: "+charSequence.toString());
                        mViewModel.customerName = charSequence.toString();
                    }
                });

        // Phone number
        RxTextView.textChanges(mBinding.phoneLayout.inputCustomerPhoneNumber)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        mViewModel.customerPhoneNumber = charSequence.toString();
                    }
                });

        // PhoneType (Spinner)
        RxAdapterView.itemSelections(mBinding.phoneLayout.inputCustomerPhoneTypeSpinner)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        mViewModel.customerPhoneType = integer.toString();
                    }
                });

        // Customer Email.
        RxTextView.textChanges(mBinding.emailLayout.inputCustomerEmail)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        mViewModel.customerEmail = charSequence.toString();
                    }
                });

        // Customer address street.
        RxTextView.textChanges(mBinding.locationLayout.inputCustomerAddressStreet)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        mViewModel.customerAddressStreet = charSequence.toString();
                    }
                });

        // Customer address country.
        RxTextView.textChanges(mBinding.locationLayout.inputCustomerAddressCountry)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        mViewModel.customerAdressCountry = charSequence.toString();
                    }
                });

        // Customer address city.
        RxTextView.textChanges(mBinding.locationLayout.inputCustomerAddressCity)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        mViewModel.customerAddressCity = charSequence.toString();
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId){
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }





    /*-----------------------------------------------------------------------------------------------------------------
    -----------------------------------------------------------------------------------------------------------------
     * LOADING CUSTOMER (for UPD Mode)
     -------------------------------------------------------------------------------------------------------------------
     -----------------------------------------------------------------------------------------------------------------*/
    private LoaderManager.LoaderCallbacks<CustomerEditViewModel> loadCustomerLoaderListener
            = new LoaderManager.LoaderCallbacks<CustomerEditViewModel>(){

        @Override
        public Loader<CustomerEditViewModel> onCreateLoader(int id, Bundle args) {
            return new LoadCustomerAsyncTaskLoader(CustomerEditActivity.this,mCustomerId);
        }

        @Override
        public void onLoadFinished(Loader<CustomerEditViewModel> loader, CustomerEditViewModel viewModelFromDB) {
            Timber.i("LoadCustomerLoader: onLoadFinished is called.");


            // If something happened when
            if(viewModelFromDB == null){
                Toast.makeText(CustomerEditActivity.this,R.string.customer_not_found,Toast.LENGTH_SHORT).show();
                return;
            }

            // If there is NOT a recovered viewModel from SavedInstance,then overwrite the one from this loader.
            // When it loads the first, it will always enter here.
            if(mViewModel.customerId == 0) mViewModel = viewModelFromDB;

            // Now set the UI with the values in ViewModel.
            setupUIFromViewModel();
        }

        @Override
        public void onLoaderReset(Loader<CustomerEditViewModel> loader) {
        }
    };


    // LOAD Customer AsyncTaskLoader.
    private static class LoadCustomerAsyncTaskLoader extends AsyncTaskLoader<CustomerEditViewModel>{

        private int customerId;

        CustomerEditViewModel cachedModelFromDB;

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if(cachedModelFromDB!=null)
                deliverResult(cachedModelFromDB);
            else
                forceLoad();
        }

        LoadCustomerAsyncTaskLoader(Context context, int customerId) {
            super(context);
            this.customerId = customerId;
        }

        @Override
        public CustomerEditViewModel loadInBackground() {
            return CustomerEditHelper.getCustomerRecordAsViewModel(getContext(),customerId);
        }

        @Override
        public void deliverResult(CustomerEditViewModel data) {
            cachedModelFromDB = data;
            super.deliverResult(cachedModelFromDB);
        }
    }







    /*-----------------------------------------------------------------------------------------------------------------
    -----------------------------------------------------------------------------------------------------------------
     * SAVE CUSTOMER LOADER (For UPD and INS Mode)
     -------------------------------------------------------------------------------------------------------------------
     -----------------------------------------------------------------------------------------------------------------*/
    private LoaderManager.LoaderCallbacks<String> saveCustomerLoaderListener
            = new LoaderManager.LoaderCallbacks<String>(){

        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            return new SaveCustomerAsyncTaskLoader(CustomerEditActivity.this,mCustomerId,mPanelMode,mViewModel);
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


    // SAVE Customer AsyncTaskLoader.
    private static class SaveCustomerAsyncTaskLoader extends AsyncTaskLoader<String>{

        private int customerId;
        private String panelMode;
        private CustomerEditViewModel viewModelToBeSaved;

        String cachedErrorMessage;

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if(cachedErrorMessage!=null)
                deliverResult(cachedErrorMessage);
            else
                forceLoad();
        }

        SaveCustomerAsyncTaskLoader(Context context, int customerId, String panelMode, CustomerEditViewModel viewModelToBeSaved) {
            super(context);
            this.customerId = customerId;
            this.panelMode  = panelMode;
            this.viewModelToBeSaved = viewModelToBeSaved;
        }

        @Override
        public String loadInBackground() {
            if(viewModelToBeSaved == null) return "An error has ocurred. Please try again.";

            // Try to Insert or Update (depending in the panel mode) the customer.
            return CustomerEditHelper.createOrUpdateCustomer(getContext(),viewModelToBeSaved,panelMode,customerId);
        }

        @Override
        public void deliverResult(String errorMessage) {
            cachedErrorMessage = errorMessage;
            super.deliverResult(cachedErrorMessage);
        }
    }
}
