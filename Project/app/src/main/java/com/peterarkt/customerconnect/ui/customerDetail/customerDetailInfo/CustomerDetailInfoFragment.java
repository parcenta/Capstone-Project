package com.peterarkt.customerconnect.ui.customerDetail.customerDetailInfo;


import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.databinding.FragmentCustomerDetailInfoBinding;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerDetailInfoFragment extends Fragment  implements LoaderManager.LoaderCallbacks<CustomerDetailInfoViewModel>{


    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static int LOAD_CUSTOMER_INFO_FROM_CUSTOMER_DETAIL_INFO = 8002;

    // Received Customer Id.
    int mCustomerId;

    //
    private FragmentCustomerDetailInfoBinding mBinding;
    private CustomerDetailInfoViewModel mViewModel;


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
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOAD_CUSTOMER_INFO_FROM_CUSTOMER_DETAIL_INFO,null,this);
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
            mBinding.phoneLayout.customerInfoPhoneNumber.setText(viewModel.customerPhoneNumber);
            mBinding.phoneLayout.customerInfoPhoneType.setText(viewModel.customerPhoneType);
            mBinding.emailLayout.customerInfoEmail.setText(viewModel.customerEmail);
            mBinding.locationLayout.customerInfoAddressStreet.setText(viewModel.customerAddressStreet);
            mBinding.locationLayout.customerInfoAddressCityAndCountry.setText(viewModel.customerCity + ", " + viewModel.customerCountry);
        }
    }

    @Override
    public void onLoaderReset(Loader<CustomerDetailInfoViewModel> loader) {

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
                    viewModel.customerAddressLatitude   = cursor.getLong(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LATITUDE));
                    viewModel.customerAddressLongitude  = cursor.getLong(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LONGITUDE));
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
