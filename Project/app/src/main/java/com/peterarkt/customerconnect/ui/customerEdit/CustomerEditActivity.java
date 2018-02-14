package com.peterarkt.customerconnect.ui.customerEdit;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.databinding.ActivityCustomerEditBinding;
import com.peterarkt.customerconnect.ui.utils.MediaUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public class CustomerEditActivity extends AppCompatActivity  implements OnMapReadyCallback {

    // For Loaders
    public static final int LOAD_CUSTOMER_LOADER_ID = 7100;
    public static final int SAVE_CUSTOMER_LOADER_ID = 7101;

    // For Camera / Attach pictures
    static final int REQUEST_IMAGE_PHOTO = 2001;

    // For SavedBundleInstance
    public static final String CUSTOMER_IS_SAVING = "CUSTOMER_IS_SAVING";
    public static final String PANEL_VIEW_MODEL   = "PANEL_VIEW_MODEL";

    //
    private String panelMode = "INS";

    // Panel status flags.
    private boolean customerIsSaving;

    // Binding.
    ActivityCustomerEditBinding mBinding;

    CustomerEditViewModel viewModel;



    // Map
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_customer_edit);


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
                getSupportLoaderManager().restartLoader(SAVE_CUSTOMER_LOADER_ID,null,saveCustomerLoaderListener);
            }
        });

        // Set Action to the "Delete Customer Photo button"
        mBinding.photoLayout.actionDeleteCustomerPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.customerPhotoPath = "";
                viewModel.photoReadyToLoad  = false;
                refreshCustomerPhotoUI();
            }
        });

        // -------------------------------------------------
        // Recover any saved state.
        // -------------------------------------------------

        // Init some values.
        viewModel = new CustomerEditViewModel();
        customerIsSaving = false;

        // Recovering values.
        if(savedInstanceState != null){
            // If customer was in the middle of saving, then init its respective loader.
            customerIsSaving = savedInstanceState.containsKey(CUSTOMER_IS_SAVING) && savedInstanceState.getBoolean(CUSTOMER_IS_SAVING);
            if (customerIsSaving)
                getSupportLoaderManager().initLoader(SAVE_CUSTOMER_LOADER_ID,null,saveCustomerLoaderListener);


            // Recovering Customer viewModel.

        }


    }

    /*
    * Map Fragment
    * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng coordinates = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(coordinates).title("Current Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CUSTOMER_IS_SAVING,customerIsSaving);
        outState.putParcelable(PANEL_VIEW_MODEL,viewModel);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey(PANEL_VIEW_MODEL))
            viewModel = savedInstanceState.getParcelable(PANEL_VIEW_MODEL);
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
                viewModel.customerPhotoPath = photoFile.getAbsolutePath();

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
        if (requestCode == REQUEST_IMAGE_PHOTO && resultCode == RESULT_OK) {
            viewModel.photoReadyToLoad = true;
            refreshCustomerPhotoUI();
        }
    }

    private void refreshCustomerPhotoUI(){
        if(viewModel.photoReadyToLoad && !viewModel.customerPhotoPath.isEmpty()){
            mBinding.photoLayout.addOrAttachPhotoContainer.setVisibility(View.GONE);
            mBinding.photoLayout.validPhotoContainer.setVisibility(View.VISIBLE);
            Timber.i("File path:" + viewModel.customerPhotoPath);
            Picasso.with(this)
                    .load("file://"+ viewModel.customerPhotoPath)
                    .error(R.drawable.ic_material_error_gray)
                    .fit()
                    .into(mBinding.photoLayout.inputCustomerPhotoImageView);
        }else{
            mBinding.photoLayout.validPhotoContainer.setVisibility(View.GONE);
            mBinding.photoLayout.addOrAttachPhotoContainer.setVisibility(View.VISIBLE);
        }
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

                    if(mBinding == null || viewModel == null ) return "An error has ocurred. Please try again.";

                    // Extracting data from the views into one single object.
                    viewModel.customerId                  = 0; // If the CustomerId is zero, internally will create an ID in INS mode.
                    viewModel.customerName                = mBinding.basicInfoLayout.inputCustomerName.getText().toString().trim();
                    viewModel.customerPhoneNumber         = mBinding.phoneLayout.inputCustomerPhoneNumber.getText().toString().trim();
                    viewModel.customerEmail               = mBinding.emailLayout.inputCustomerEmail.getText().toString().trim();
                    viewModel.customerPhoneType           = String.valueOf(mBinding.phoneLayout.inputCustomerPhoneTypeSpinner.getSelectedItemId());
                    viewModel.customerAddressStreet       = mBinding.locationLayout.inputCustomerAddressStreet.getText().toString();
                    viewModel.customerAddressCity         = mBinding.locationLayout.inputCustomerAddressCity.getText().toString();
                    viewModel.customerAdressCountry       = mBinding.locationLayout.inputCustomerAddressCountry.getText().toString();
                    // ... latitude, longitude and photo path were set in another part.

                    return CustomerEditHelper.createCustomer(CustomerEditActivity.this,viewModel);
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
