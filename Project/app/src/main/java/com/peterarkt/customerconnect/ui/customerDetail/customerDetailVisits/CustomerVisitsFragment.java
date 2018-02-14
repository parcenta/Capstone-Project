package com.peterarkt.customerconnect.ui.customerDetail.customerDetailVisits;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.VisitContract;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.databinding.FragmentCustomerVisitsBinding;
import com.peterarkt.customerconnect.ui.utils.CustomerConnectUIUtils;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailVisits.customerNewVisitDialog.CustomerNewVisitDialogFragment;

import java.util.Date;

import timber.log.Timber;

public class CustomerVisitsFragment extends Fragment  implements LoaderManager.LoaderCallbacks<CustomerVisitsViewModel>{


    private static final String CUSTOMER_ID = "CUSTOMER_ID";
    private static final int LOAD_CUSTOMER_VISITS = 8003;

    // Received Customer Id.
    int mCustomerId;

    //
    private FragmentCustomerVisitsBinding mBinding;
    private CustomerVisitsViewModel mViewModel;
    private CustomerVisitsAdapter mAdapter;

    private CustomerNewVisitDialogFragment newVisitDialogFragment;

    public CustomerVisitsFragment() {
    }

    /* --------------------------------------
    * Instance Helpers
    * --------------------------------------*/
    public static CustomerVisitsFragment newInstance(int customerId) {
        Bundle arguments = new Bundle();
        arguments.putInt(CUSTOMER_ID, customerId);
        CustomerVisitsFragment fragment = new CustomerVisitsFragment();
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
    * OnActivityCreated
    * --------------------------------------*/
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set FAB action.
        mBinding.actionNewVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Creating the New Visit Dialog Fragment.
                newVisitDialogFragment = CustomerNewVisitDialogFragment.newInstance(mCustomerId);

                // Set on Dismiss Listener.
                setOnDismissListenerOnNewVisitDialog();

                // Showing the dialog.
                newVisitDialogFragment.show(getFragmentManager(), "newVisitDialog");
            }
        });

        // If DialogFragment was opened during the Screen rotation, then we must get it and refresh
        // the dismiss listener.
        if(savedInstanceState != null) {
            newVisitDialogFragment = (CustomerNewVisitDialogFragment) getFragmentManager().findFragmentByTag("newVisitDialog");
            if (newVisitDialogFragment != null) setOnDismissListenerOnNewVisitDialog();
        }
    }

    /* --------------------------------------
    * OnCreateView
    * --------------------------------------*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_visits, container, false);

        // Set Adapter
        mAdapter = new CustomerVisitsAdapter(null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mBinding.customerDetailVisitsRecyclerView.setLayoutManager(layoutManager);
        mBinding.customerDetailVisitsRecyclerView.setAdapter(mAdapter);

        return mBinding.getRoot();
    }

    /* --------------------------------------
    * OnResume
    * --------------------------------------*/
    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOAD_CUSTOMER_VISITS,null,this);
    }



    // Set an OnDismissListener in order to refresh the Customer Visit list when the
    // dialog is dismissed.
    // Source: https://stackoverflow.com/questions/9853430/refresh-fragment-when-dialogfragment-is-dismissed
    private void setOnDismissListenerOnNewVisitDialog(){
        if(newVisitDialogFragment==null) return;

        newVisitDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // Just CHecking there is an activity attached.
                Context context = getActivity();
                if(context == null) return;

                // Restart loader
                getLoaderManager().restartLoader(LOAD_CUSTOMER_VISITS,null,CustomerVisitsFragment.this);
            }
        });
    }

    /* --------------------------------------
    * Loader methods
    * --------------------------------------*/
    @Override
    public Loader<CustomerVisitsViewModel> onCreateLoader(int i, Bundle bundle) {
        return new CustomerVisitsLoader(getActivity(),mCustomerId);
    }

    @Override
    public void onLoadFinished(Loader<CustomerVisitsViewModel> loader, CustomerVisitsViewModel viewModel) {
        mViewModel = viewModel;
        mAdapter.setItemList(viewModel.visitsList);
        if(mViewModel.visitsList.size()>0){
            mBinding.noVisitsFoundTextView.setVisibility(View.GONE);
            mBinding.customerDetailVisitsRecyclerView.setVisibility(View.VISIBLE);
        }else {
            mBinding.customerDetailVisitsRecyclerView.setVisibility(View.VISIBLE);
            mBinding.noVisitsFoundTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<CustomerVisitsViewModel> loader) {
        mViewModel = null;
    }

    /* ------------------------------------------------------------------------------------------------
     *  AsyncTaskLoader for Loading the Customer's Visits.
       ------------------------------------------------------------------------------------------------*/
    private static class CustomerVisitsLoader extends AsyncTaskLoader<CustomerVisitsViewModel> {

        private CustomerVisitsViewModel cachedViewModel;
        private int customerId;

        CustomerVisitsLoader(Context context, int customerId){
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
        public CustomerVisitsViewModel loadInBackground() {

            CustomerVisitsViewModel viewModel = new CustomerVisitsViewModel();


            // Context null check.
            Context context = getContext();
            if (context == null) {
                Timber.d("An error ocurred. Context is null...");
                return null;
            }

            // Get Customer Visits (ordered from most recent to last recent)
            Cursor cursor = CustomerDBUtils.getCustomerVisitsListRecord(context,customerId, VisitContract.VisitEntry.COLUMN_VISIT_DATETIME + " desc");

            if(cursor!=null){
                // If cursor has results...
                while (cursor.moveToNext()){

                    // Get Date from the record.
                    long visitDateAsLong = cursor.getLong(cursor.getColumnIndex(VisitContract.VisitEntry.COLUMN_VISIT_DATETIME));
                    Date visitDate = new Date(visitDateAsLong);
                    String visitDateAsString = CustomerConnectUIUtils.getDateAsMMMddYYYY(visitDate);

                    // Get Commentary
                    String visitCommentary = cursor.getString(cursor.getColumnIndex(VisitContract.VisitEntry.COLUMN_VISIT_COMMENTARY));

                    viewModel.visitsList.add(new CustomerVisit(visitDateAsString,visitCommentary));
                }

                // Close the cursor.
                if(!cursor.isClosed()) cursor.close();
            }


            return viewModel;
        }

        @Override
        public void deliverResult(CustomerVisitsViewModel viewModel){
            cachedViewModel = viewModel;
            super.deliverResult(cachedViewModel);
        }
    }

}
