package com.peterarkt.customerconnect.ui.customersList;


import android.content.Context;
import android.content.Intent;
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

import com.jakewharton.rxbinding.widget.RxTextView;
import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.databinding.FragmentCustomerListBinding;
import com.peterarkt.customerconnect.ui.customerEdit.CustomerEditActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<CustomerItem>>,CustomerListAdapter.CustomerOnClickHandler {

    // Loader Id
    private final static int LOADER_CUSTOMER_SEARCH_ID = 7001;

    //
    private final static String TEXT_TO_SEARCH = "TEXT_TO_SEARCH";

    FragmentCustomerListBinding mBinding;

    // For the Customer list´ RecyclerView
    List<CustomerItem> mItemList;
    CustomerListAdapter mAdapter;
    private String mTextToSearch;

    // Empty Constructor
    public CustomerListFragment(){
    }


    // Fragment "newInstance" method.
    public static CustomerListFragment newInstance(){
        CustomerListFragment fragment = new CustomerListFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_list, container, false);


        // Set Adapter
        mAdapter = new CustomerListAdapter(getActivity(),null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mBinding.customerListRecyclerView.setLayoutManager(layoutManager);
        mBinding.customerListRecyclerView.setAdapter(mAdapter);

        // Recovering the textToSearch.
        mTextToSearch = "";
        if(savedInstanceState!=null && savedInstanceState.containsKey(TEXT_TO_SEARCH))
            mTextToSearch = savedInstanceState.getString(TEXT_TO_SEARCH);

        // Set the Text Change listener.
        RxTextView.textChanges(mBinding.searchText)
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        searchCustomer(charSequence.toString());
                    }
                });

        mBinding.actionNewCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CustomerEditActivity.class));
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        searchCustomer(mTextToSearch);
    }

    private void searchCustomer(String textToSearch){
        mTextToSearch = textToSearch;

        Bundle searchBundle = new Bundle();
        searchBundle.putString(TEXT_TO_SEARCH,mTextToSearch);
        getLoaderManager().restartLoader(LOADER_CUSTOMER_SEARCH_ID, searchBundle,this);
    }

    /* ------------------------------------------------------------------------------------------------
         *  Loader Methods.
           ------------------------------------------------------------------------------------------------*/
    @Override
    public Loader<List<CustomerItem>> onCreateLoader(int id, Bundle args) {
        return new CustomerListAsyncTaskLoader(getActivity(),args);
    }

    @Override
    public void onLoadFinished(Loader<List<CustomerItem>> loader, List<CustomerItem> itemList) {

        // Set the cursor.
        mItemList = itemList;

        // If there are results, then show the list...
        if(mItemList!=null && mItemList.size() > 0){
            mBinding.noCustomerFoundContainer.setVisibility(View.GONE);
            mBinding.customerListRecyclerView.setVisibility(View.VISIBLE);
        }else{ // If there are no results, then show only the "No Customers Found" message.
            mBinding.customerListRecyclerView.setVisibility(View.GONE);
            mBinding.noCustomerFoundContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<CustomerItem>> loader) {
        mItemList = null;
    }



    /* ------------------------------------------------------------------------------------------------
     *  CustomerClick method (from Adapter´ interface).
       ------------------------------------------------------------------------------------------------*/
    @Override
    public void onCustomerClick(int customerId) {

    }


    /* ------------------------------------------------------------------------------------------------
     *  AsyncTaskLoader
       ------------------------------------------------------------------------------------------------*/
    private static class CustomerListAsyncTaskLoader extends AsyncTaskLoader<List<CustomerItem>>{

        private List<CustomerItem> cachedItemList;
        private Bundle mArgs;

        CustomerListAsyncTaskLoader(Context context,Bundle args){
            super(context);
            mArgs = args;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            if(cachedItemList != null)
                deliverResult(cachedItemList);
            else
                forceLoad();
        }

        @Override
        public List<CustomerItem> loadInBackground() {

            List<CustomerItem> itemList = new ArrayList<>();

            Context context = getContext();
            if (context == null) {
                Timber.d("An error ocurred. Context is null...");
                return itemList;
            }

            String textToSearch = "";
            if(mArgs != null && mArgs.containsKey(TEXT_TO_SEARCH)) textToSearch = mArgs.getString(TEXT_TO_SEARCH);

            // Search for the Customers.
            Timber.d("Searching customer with '"+textToSearch+"'...");
            Cursor cursor = context.getContentResolver().query(CustomerContract.CustomerEntry.CONTENT_URI,
                                                                null,
                                                                CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME + " LIKE ? ",
                                                                new String[]{textToSearch},
                                                                CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME);

            if(cursor!=null){
                // If cursor has results...
                if(cursor.getCount() > 0){

                    // Move to the first result...
                    cursor.moveToFirst();

                    // Loop the results.
                    do{
                        // Get values from the Cursor
                        int customerId                  = cursor.getInt(cursor.getColumnIndex(CustomerContract.CustomerEntry._ID));
                        String customerName             = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME));
                        String customerAddressStreet    = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_ADDRESS_STREET));
                        String customerPhotoUrl         = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHOTO_PATH));

                        // Add to the list.
                        itemList.add(new CustomerItem(customerId,customerName,customerAddressStreet,customerPhotoUrl));

                    }while (cursor.moveToNext());
                }

                // Close the cursor.
                if(!cursor.isClosed()) cursor.close();
            }


            return itemList;
        }

        @Override
        public void deliverResult(List<CustomerItem> itemList){
            cachedItemList = itemList;
            super.deliverResult(cachedItemList);
        }
    }
}
