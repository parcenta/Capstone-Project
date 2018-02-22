package com.peterarkt.customerconnect.ui.customerDetail.customerDetailInfo;


import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.databinding.FragmentCustomerDetailInfoBinding;
import com.peterarkt.customerconnect.ui.utils.Constants;
import com.peterarkt.customerconnect.ui.utils.PhoneActionUtils;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerDetailInfoFragment extends Fragment  implements LoaderManager.LoaderCallbacks<CustomerDetailInfoViewModel>, OnMapReadyCallback {


    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final int LOAD_CUSTOMER_INFO_FROM_CUSTOMER_DETAIL_INFO = 8002;

    // Received Customer Id.
    int mCustomerId;

    //
    private FragmentCustomerDetailInfoBinding mBinding;
    private CustomerDetailInfoViewModel mViewModel;

    private GoogleMap mGoogleMap;


    public CustomerDetailInfoFragment() {
        // Required empty public constructor
    }

    /* --------------------------------------
    * Instance Helpers
    * --------------------------------------*/

    public static CustomerDetailInfoFragment newInstance(int customerId) {
        Bundle arguments = new Bundle();
        arguments.putInt(CUSTOMER_ID, customerId);
        CustomerDetailInfoFragment fragment = new CustomerDetailInfoFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle receivedBundle = getArguments();
        if (receivedBundle.containsKey(CUSTOMER_ID)) mCustomerId = receivedBundle.getInt(CUSTOMER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_detail_info, container, false);

        // Phone Call action
        mBinding.phoneLayout.actionCallPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mViewModel!=null) PhoneActionUtils.makePhoneCall(getActivity(),mViewModel.customerPhoneNumber);
            }
        });

        // Send action email
        mBinding.emailLayout.actionSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mViewModel!=null) PhoneActionUtils.sendEmail(getActivity(),mViewModel.customerEmail);
            }
        });

        // Navigate to coordinates
        mBinding.locationLayout.actionNavigateToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mViewModel != null) PhoneActionUtils.navigateToCoordinates(getActivity(),mViewModel.customerAddressLatitude,mViewModel.customerAddressLongitude);
            }
        });

        // Set the map
        // Set the Map Fragment (Loading the Map fragment this way, avoid that it delays the first time that it opens)
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.customer_info_map_holder);
        if (mapFragment == null) {
            FragmentManager fm = getChildFragmentManager();
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.customer_info_map_holder, mapFragment, "detailMapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(this);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // We restart to show any changes if the customer was updated or not.
        getLoaderManager().restartLoader(LOAD_CUSTOMER_INFO_FROM_CUSTOMER_DETAIL_INFO,null,this);
    }

    /* ------------------------------------------------------
            *   Loader Methods
            * ------------------------------------------------------*/
    @Override
    public Loader<CustomerDetailInfoViewModel> onCreateLoader(int id, Bundle args) {
        return new CustomerDetailInfoAsyncTaskLoader(getActivity(),mCustomerId);
    }

    @Override
    public void onLoadFinished(Loader<CustomerDetailInfoViewModel> loader, CustomerDetailInfoViewModel viewModel) {
        mViewModel = viewModel;

        if(mViewModel != null){

            // Email
            mBinding.emailLayout.customerInfoEmail.setText(!viewModel.customerEmail.isEmpty() ? viewModel.customerEmail : getString(R.string.unknown));

            // Set the phone number and type.
            if(mViewModel.customerPhoneNumber.isEmpty()){
                mBinding.phoneLayout.customerInfoPhoneNumber.setText(getString(R.string.unknown));
                mBinding.phoneLayout.customerInfoPhoneType.setText("-");
            }else{
                mBinding.phoneLayout.customerInfoPhoneNumber.setText(mViewModel.customerPhoneNumber);

                try {
                    int addressTypePosition = Integer.valueOf(mViewModel.customerPhoneType);
                    String addressTypeDescription = Constants.getAddressTypeDescription(getActivity(),addressTypePosition);
                    mBinding.phoneLayout.customerInfoPhoneType.setText(addressTypeDescription);
                }catch (Exception e){
                    e.printStackTrace();
                    mBinding.phoneLayout.customerInfoPhoneType.setText(getString(R.string.error));
                }
            }


            // Location
            mBinding.locationLayout.customerInfoAddressStreet.setText(viewModel.customerAddressStreet);
            refreshMap();

            // Set the country and city.
            if(!viewModel.customerCountry.isEmpty() && !viewModel.customerCity.isEmpty())
                mBinding.locationLayout.customerInfoAddressCityAndCountry.setText(viewModel.customerCity + ", " + viewModel.customerCountry);
            else if (!viewModel.customerCountry.isEmpty())
                mBinding.locationLayout.customerInfoAddressCityAndCountry.setText(viewModel.customerCountry);
            else if (!viewModel.customerCity.isEmpty())
                mBinding.locationLayout.customerInfoAddressCityAndCountry.setText(viewModel.customerCity);
            else
                mBinding.locationLayout.customerInfoAddressCityAndCountry.setText("N/A");

        }
    }

    @Override
    public void onLoaderReset(Loader<CustomerDetailInfoViewModel> loader) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        refreshMap();
    }

    private void refreshMap() {
        if (mGoogleMap == null || mViewModel == null) return;

        // Clearing the map.
        mGoogleMap.clear();

        // If there is a valid Latitude and Longitude, then show the marker in that position.
        if (mViewModel.customerAddressLatitude != 0.00 || mViewModel.customerAddressLongitude != 0.00) {
            LatLng coordinates = new LatLng(mViewModel.customerAddressLatitude, mViewModel.customerAddressLongitude);
            mGoogleMap.addMarker(new MarkerOptions().position(coordinates).title("Position"));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 16));
        }
    }


    /* ------------------------------------------------------------------------------------------------
     *  AsyncTaskLoader
       ------------------------------------------------------------------------------------------------*/
    private static class CustomerDetailInfoAsyncTaskLoader extends AsyncTaskLoader<CustomerDetailInfoViewModel> {

        private CustomerDetailInfoViewModel cachedViewModel;
        private int customerId;

        CustomerDetailInfoAsyncTaskLoader(Context context, int customerId){
            super(context);
            this.customerId = customerId;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            if(cachedViewModel != null)
                deliverResult(cachedViewModel);
            else
                forceLoad();
        }

        @Override
        public CustomerDetailInfoViewModel loadInBackground() {

            CustomerDetailInfoViewModel viewModel = new CustomerDetailInfoViewModel();


            // Context null check.
            Context context = getContext();
            if (context == null) {
                Timber.d("An error ocurred. Context is null...");
                return null;
            }

            // Get Customer Record cursor.
            Cursor cursor = CustomerDBUtils.getCustomerRecord(context,customerId);

            if(cursor!=null){
                // If cursor has results...
                if(cursor.moveToFirst()){
                    viewModel.customerPhoneNumber       = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHONE_NUMBER));
                    viewModel.customerPhoneType         = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHONE_TYPE));
                    viewModel.customerEmail             = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_EMAIL));
                    viewModel.customerAddressStreet     = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_ADDRESS_STREET));
                    viewModel.customerCity              = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_CITY));
                    viewModel.customerCountry           = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_COUNTRY));
                    viewModel.customerAddressLatitude   = cursor.getDouble(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LATITUDE));
                    viewModel.customerAddressLongitude  = cursor.getDouble(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LONGITUDE));
                }

                // Close the cursor.
                if(!cursor.isClosed()) cursor.close();
            }


            return viewModel;
        }

        @Override
        public void deliverResult(CustomerDetailInfoViewModel viewModel){
            cachedViewModel = viewModel;
            super.deliverResult(cachedViewModel);
        }
    }
}
