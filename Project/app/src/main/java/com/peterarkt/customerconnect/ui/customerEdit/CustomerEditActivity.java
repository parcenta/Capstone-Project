package com.peterarkt.customerconnect.ui.customerEdit;


import android.databinding.DataBindingUtil;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
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

public class CustomerEditActivity extends AppCompatActivity  implements OnMapReadyCallback {

    // For Loaders
    public static final int LOAD_CUSTOMER_LOADER_ID = 7100;
    public static final int SAVE_CUSTOMER_LOADER_ID = 7101;

    // For SavedBundleInstance
    public static final String CUSTOMER_IS_SAVING = "CUSTOMER_IS_SAVING";

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

        // Set Action to the "Save Button".
        mBinding.actionSaveCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerIsSaving = true;
                getSupportLoaderManager().restartLoader(SAVE_CUSTOMER_LOADER_ID,null,saveCustomerLoaderListener);
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
        super.onSaveInstanceState(outState);
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
