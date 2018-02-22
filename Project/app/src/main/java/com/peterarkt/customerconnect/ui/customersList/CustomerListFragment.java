package com.peterarkt.customerconnect.ui.customersList;


import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.peterarkt.customerconnect.ui.CustomerConnectMainActivityHandler;
import com.peterarkt.customerconnect.ui.customerEdit.CustomerEditActivity;
import com.peterarkt.customerconnect.ui.utils.Constants;

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

    private final static String LIST_STATE_KEY = "LIST_STATE_KEY";

    //
    private final static String TEXT_TO_SEARCH = "TEXT_TO_SEARCH";

    FragmentCustomerListBinding mBinding;

    // Handler to comunicate to the parent activity.
    CustomerConnectMainActivityHandler mActivityHandler;

    // For the Customer list´ RecyclerView
    List<CustomerItem> mItemList;
    CustomerListAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    Parcelable mListState;

    private String mTextToSearch = "";

    // Empty Constructor
    public CustomerListFragment(){
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Binding the view.
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_list, container, false);


        // Set Adapter
        mAdapter = new CustomerListAdapter(getActivity(),null, this);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mBinding.customerListRecyclerView.setLayoutManager(mLayoutManager);
        mBinding.customerListRecyclerView.setAdapter(mAdapter);

        // Set action to "New customer" FAB.
        mBinding.actionNewCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomerEditActivity.launch(getActivity(), Constants.INSERT_MODE,0);
            }
        });

        // Recovering LayoutManager state.
        if(savedInstanceState!=null){
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        searchCustomer(mBinding.searchText.getText().toString());
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mActivityHandler         = (CustomerConnectMainActivityHandler) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement RecipeDetailFragmentStepAdapter.OnRecipeStepClickHandler AND RecipeDetailHandler");
        }
    }

    private void searchCustomer(String textToSearch){
        mTextToSearch = textToSearch;
        Bundle searchBundle = new Bundle();
        searchBundle.putString(TEXT_TO_SEARCH,textToSearch);

        // If Fragment is attached to the activity, then restart the search...
        if(isAdded()) getLoaderManager().restartLoader(LOADER_CUSTOMER_SEARCH_ID, searchBundle,this);
    }

    // Called from parent activity.
    public void restartSearchFromParentActivity(){
        if(mTextToSearch!=null) searchCustomer(mTextToSearch);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        mAdapter.setItemList(mItemList);
        if (mListState != null) mLayoutManager.onRestoreInstanceState(mListState);

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
        mAdapter.setItemList(null);
    }

    /* ------------------------------------------------------------------------------------------------
     *  OnSaveInstance/OnRestoreInstance.
       ------------------------------------------------------------------------------------------------*/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save list state
        mListState = mLayoutManager.onSaveInstanceState();
        if(mListState!=null) outState.putParcelable(LIST_STATE_KEY, mListState);
    }

    /* ------------------------------------------------------------------------------------------------
     *  CustomerClick method (from Adapter´ interface).
       ------------------------------------------------------------------------------------------------*/
    @Override
    public void onCustomerClick(int customerId) {
        if(mActivityHandler!=null) mActivityHandler.showCustomerSelected(customerId);
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

            // Context null check.
            Context context = getContext();
            if (context == null) {
                Timber.d("An error ocurred. Context is null...");
                return itemList;
            }

            // If there is a text to be searched, then must be added to the query...
            String textToSearch = "";
            String selection = null;
            String[] selectionArgs = null;
            if(mArgs != null && mArgs.containsKey(TEXT_TO_SEARCH)) {

                // Extract the text to search from the bundle.
                textToSearch = mArgs.getString(TEXT_TO_SEARCH,"");

                // If there is some text to be searched, then add to the filter (selection and selectionArgs)...
                if(!textToSearch.trim().isEmpty()){
                    selection = CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME + " LIKE ?";
                    selectionArgs = new String[]{"%" + textToSearch + "%"};
                }
            }

            // Search for the Customers.
            Timber.d("Searching customer with '"+textToSearch+"'...");
            Cursor cursor = context.getContentResolver().query(CustomerContract.CustomerEntry.CONTENT_URI,
                                                                null,
                                                                selection,
                                                                selectionArgs,
                                                                CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME);

            if(cursor!=null){
                // If cursor has results...
                if(cursor.getCount() > 0){

                    // Loop the results.
                    while (cursor.moveToNext()){
                        // Get values from the Cursor
                        int customerId                  = cursor.getInt(cursor.getColumnIndex(CustomerContract.CustomerEntry._ID));
                        String customerName             = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME));
                        String customerAddressStreet    = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_ADDRESS_STREET));
                        String customerPhotoUrl         = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHOTO_PATH));

                        Timber.i("Loading CustomerName: " + customerName + " - Path: " + customerPhotoUrl);

                        // Add to the list.
                        itemList.add(new CustomerItem(customerId,customerName,customerAddressStreet,customerPhotoUrl));

                    }
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
