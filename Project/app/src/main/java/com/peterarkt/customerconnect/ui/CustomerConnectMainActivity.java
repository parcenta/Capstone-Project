package com.peterarkt.customerconnect.ui;


import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.databinding.ActivityCustomerConnectMainBinding;
import com.peterarkt.customerconnect.ui.customerDetail.CustomerDetailActivity;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailHeader.CustomerDetailHeaderFragment;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailInfo.CustomerDetailInfoFragment;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailVisits.CustomerVisitsFragment;
import com.peterarkt.customerconnect.ui.customersList.CustomerListFragment;
import com.peterarkt.customerconnect.ui.widget.WidgetIntentService;

import timber.log.Timber;

public class CustomerConnectMainActivity extends AppCompatActivity implements CustomerConnectMainActivityHandler{

    ActivityCustomerConnectMainBinding mBinding;

    AsyncTask mDeleteCustomerAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binding the view.
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_customer_connect_main);

        // Set toolbar
        setSupportActionBar(mBinding.toolbar);


        // ----------------------------------------------------------------------------
        // FOR TABLETS: Check if we are already showing a selected customer fragments.
        // ----------------------------------------------------------------------------
        boolean hasCustomerDetailFragmentsContainer = findViewById(R.id.customer_detail_container) != null;
        if(savedInstanceState != null && hasCustomerDetailFragmentsContainer){
            FragmentManager fm = getSupportFragmentManager();
            Fragment customerDetailHeaderFragment   = fm.findFragmentByTag("customerHeaderFragment");

            // If there are customer detail´s fragments being shown, then show the customer detail fragment container.
            if(customerDetailHeaderFragment != null){
                Timber.d("No detail being shown...");
                mBinding.noSelectedCustomerContainer.setVisibility(View.GONE);
                mBinding.selectedCustomerDetailContainer.setVisibility(View.VISIBLE);
            }else // If not, then show the user a message indicating to select a customer.
            {
                Timber.d("No detail being shown...");
                mBinding.selectedCustomerDetailContainer.setVisibility(View.GONE);
                mBinding.noSelectedCustomerContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void showCustomerSelected(int customerId) {

        // Detect if fragment containers are present (is equivalent to ask if it is a tablet).
        boolean hasCustomerDetailFragmentsContainer = findViewById(R.id.customer_detail_container) != null;

        // If is Tablet (sw600dp)
        if(hasCustomerDetailFragmentsContainer){
            FragmentManager fm = getSupportFragmentManager();

            // Create the fragments for the Customer Detail.
            CustomerDetailHeaderFragment customerDetailHeaderFragment = CustomerDetailHeaderFragment.newInstance(customerId);
            CustomerDetailInfoFragment customerDetailInfoFragment = CustomerDetailInfoFragment.newInstance(customerId);
            CustomerVisitsFragment customerDetailVisitsFragment = CustomerVisitsFragment.newInstance(customerId);

            // Hide No Customer Selected container.
            mBinding.noSelectedCustomerContainer.setVisibility(View.GONE);

            // Show Customer´ Fragment Container.
            mBinding.noSelectedCustomerContainer.setVisibility(View.GONE);

            // Now replace them in their respective places.
            fm.beginTransaction().replace(R.id.customer_detail_header_fragment_holder, customerDetailHeaderFragment,"customerHeaderFragment")
                                .replace(R.id.customer_detail_info_fragment_holder,customerDetailInfoFragment,"customerInfoFragment")
                                .replace(R.id.customer_detail_visits_fragment_holder,customerDetailVisitsFragment,"customerVisitFragment").commit();
        }else{
            CustomerDetailActivity.launch(this,customerId);
        }
    }



    // -------------------------------------------------------------------
    // From where is called, it will be called only on Tablet Mode.
    // -------------------------------------------------------------------
    @Override
    public void deleteCustomer(final int customerId) {

        // First remove the Fragments of the customer that is going to be deleted.
        try {
            FragmentManager fm = getSupportFragmentManager();

            // Create the fragments for the Customer Detail.
            Fragment customerDetailHeaderFragment   = fm.findFragmentByTag("customerHeaderFragment");
            Fragment customerDetailInfoFragment     = fm.findFragmentByTag("customerInfoFragment");
            Fragment customerDetailVisitsFragment   = fm.findFragmentByTag("customerVisitFragment");

            // Now remove the fragments of the deleted customer.
            if(customerDetailHeaderFragment!=null) {
                fm.beginTransaction().remove(customerDetailHeaderFragment)
                        .remove(customerDetailInfoFragment)
                        .remove(customerDetailVisitsFragment).commit();
            }

        }catch (Exception e){
            e.printStackTrace();
        }


        // Cancel any previous asynctask.
        if(mDeleteCustomerAsyncTask!=null && !mDeleteCustomerAsyncTask.isCancelled()) mDeleteCustomerAsyncTask.cancel(true);

        // Initiating an asynctask.
        mDeleteCustomerAsyncTask = new AsyncTask<Object,Void,String>() {
            @Override
            protected String doInBackground(Object[] objects) {

                // Check if the deletion was correct.
                boolean deletionSuccessfully = CustomerDBUtils.deleteCustomerAndVisits(CustomerConnectMainActivity.this,customerId);

                // If it was correct then I return an empty error message. If not, then i return a "error ocurred" message.
                return deletionSuccessfully ? "" : getString(R.string.an_error_has_ocurred);
            }

            @Override
            protected void onPostExecute(String errorMessage) {
                super.onPostExecute(errorMessage);

                try {
                    CustomerConnectMainActivity activity = CustomerConnectMainActivity.this;

                    // If there was no error message...
                    if (errorMessage.isEmpty()) {
                        // Show success toast message.
                        Toast.makeText(activity, R.string.customer_deleted_successfully, Toast.LENGTH_SHORT).show();

                        // Refresh widget
                        WidgetIntentService.startActionShowTodaysVisits(activity);

                        // Refresh the list of customers in the CustomerListFragment.
                        FragmentManager fm = activity.getSupportFragmentManager();
                        CustomerListFragment customerListFragment = (CustomerListFragment) fm.findFragmentById(R.id.customer_list_fragment_holder);
                        if(customerListFragment!=null) customerListFragment.restartSearchFromParentActivity();
                    }else {
                        // Show success toast message.
                        Toast.makeText(activity, R.string.an_error_has_ocurred, Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };

        // Execute async task.
        mDeleteCustomerAsyncTask.execute();

    }
}
