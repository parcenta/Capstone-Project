package com.peterarkt.customerconnect.ui.customerDetail.customerDetailHeader;


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
import com.peterarkt.customerconnect.databinding.FragmentCustomerDetailHeaderBinding;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerDetailHeaderFragment extends Fragment implements LoaderManager.LoaderCallbacks<CustomerDetailHeaderViewModel>{

    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static int LOAD_CUSTOMER_INFO_FROM_CUSTOMER_DETAIL_HEADER = 8001;

    // Received Customer Id.
    int mCustomerId;

    //
    private FragmentCustomerDetailHeaderBinding mBinding;
    private CustomerDetailHeaderViewModel mViewModel;


    public CustomerDetailHeaderFragment() {
        // Required empty public constructor
    }

    /* --------------------------------------
    * Instance Helpers
    * --------------------------------------*/

    public static CustomerDetailHeaderFragment newInstance(int customerId) {
        Bundle arguments = new Bundle();
        arguments.putInt(CUSTOMER_ID, customerId);
        CustomerDetailHeaderFragment fragment = new CustomerDetailHeaderFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /* --------------------------------------
    * OnCreate
    * --------------------------------------*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle receivedBundle = getArguments();
        if (receivedBundle.containsKey(CUSTOMER_ID)) mCustomerId = receivedBundle.getInt(CUSTOMER_ID);

    }


    /* --------------------------------------
    * OnCreateView
    * --------------------------------------*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_detail_header, container, false);
        return mBinding.getRoot();
    }

    /* --------------------------------------
    * OnCreateView
    * --------------------------------------*/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOAD_CUSTOMER_INFO_FROM_CUSTOMER_DETAIL_HEADER,null,this);
    }

    /* --------------------------------------
        * Loader methods
        * --------------------------------------*/
    @Override
    public Loader<CustomerDetailHeaderViewModel> onCreateLoader(int i, Bundle bundle) {
        return new CustomerDetailHeaderAsyncTaskLoader(getActivity(),mCustomerId);
    }

    @Override
    public void onLoadFinished(Loader<CustomerDetailHeaderViewModel> loader, CustomerDetailHeaderViewModel viewModel) {

        mViewModel = viewModel;

        if(mViewModel != null){
            mBinding.customerNameTextView.setText(viewModel.customerName);
            if(!viewModel.customerPhotoPath.isEmpty()){
                Picasso.with(getActivity())
                        .load(viewModel.customerPhotoPath)
                        .error(R.drawable.ic_material_person_gray)
                        .into(mBinding.customerPhotoBackgroundImageView);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<CustomerDetailHeaderViewModel> loader) {

    }



    /* ------------------------------------------------------------------------------------------------
     *  AsyncTaskLoader
       ------------------------------------------------------------------------------------------------*/
    private static class CustomerDetailHeaderAsyncTaskLoader extends AsyncTaskLoader<CustomerDetailHeaderViewModel> {

        private CustomerDetailHeaderViewModel cachedViewModel;
        private int customerId;

        CustomerDetailHeaderAsyncTaskLoader(Context context, int customerId){
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
        public CustomerDetailHeaderViewModel loadInBackground() {

            CustomerDetailHeaderViewModel viewModel = new CustomerDetailHeaderViewModel();


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
                    viewModel.customerName      = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME));
                    viewModel.customerPhotoPath = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHOTO_PATH));
                }

                // Close the cursor.
                if(!cursor.isClosed()) cursor.close();
            }


            return viewModel;
        }

        @Override
        public void deliverResult(CustomerDetailHeaderViewModel viewModel){
            cachedViewModel = viewModel;
            super.deliverResult(cachedViewModel);
        }
    }
}