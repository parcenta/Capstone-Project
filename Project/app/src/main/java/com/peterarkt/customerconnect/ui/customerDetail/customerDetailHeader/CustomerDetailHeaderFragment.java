package com.peterarkt.customerconnect.ui.customerDetail.customerDetailHeader;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.databinding.FragmentCustomerDetailHeaderBinding;
import com.peterarkt.customerconnect.ui.CustomerConnectMainActivity;
import com.peterarkt.customerconnect.ui.CustomerConnectMainActivityHandler;
import com.peterarkt.customerconnect.ui.customerDetail.CustomerDetailActivity;
import com.peterarkt.customerconnect.ui.customerEdit.CustomerEditActivity;
import com.peterarkt.customerconnect.ui.utils.Constants;
import com.peterarkt.customerconnect.ui.utils.PhoneActionUtils;
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

    CustomerConnectMainActivityHandler mParentActivityHandler;


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

        setHasOptionsMenu(true);

        Bundle receivedBundle = getArguments();
        if (receivedBundle.containsKey(CUSTOMER_ID)) mCustomerId = receivedBundle.getInt(CUSTOMER_ID);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId){
            case R.id.menu_edit_customer:
                CustomerEditActivity.launch(getActivity(), Constants.UPDATE_MODE,mCustomerId);
                return true;
            case R.id.menu_zoom_customer_image:
                if(mViewModel != null) PhoneActionUtils.openImage(getActivity(),mViewModel.customerPhotoPath);
                return true;
            case R.id.menu_delete_customer:
                if(mParentActivityHandler!=null) mParentActivityHandler.deleteCustomer(mCustomerId);
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mParentActivityHandler         = (CustomerConnectMainActivityHandler) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement RecipeDetailFragmentStepAdapter.OnRecipeStepClickHandler AND RecipeDetailHandler");
        }
    }

    /* --------------------------------------
    * OnCreateView
    * --------------------------------------*/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOAD_CUSTOMER_INFO_FROM_CUSTOMER_DETAIL_HEADER,null,this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.customer_detail_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
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
                        .load("file://" + viewModel.customerPhotoPath)
                        .error(R.drawable.ic_material_person_gray)
                        .fit()
                        .centerCrop()
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
