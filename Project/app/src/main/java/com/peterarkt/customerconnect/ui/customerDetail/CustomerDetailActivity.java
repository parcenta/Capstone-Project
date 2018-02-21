package com.peterarkt.customerconnect.ui.customerDetail;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.databinding.ActivityCustomerDetailBinding;
import com.peterarkt.customerconnect.ui.CustomerConnectMainActivityHandler;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailHeader.CustomerDetailHeaderFragment;

import timber.log.Timber;

public class CustomerDetailActivity extends AppCompatActivity implements CustomerConnectMainActivityHandler{

    private final static String CUSTOMER_ID = "CUSTOMER_ID";

    private int mCustomerId;

    private ActivityCustomerDetailBinding mBinding;

    CustomerDetailPagerAdapter mPagerAdapter;

    private AsyncTask mDeleteCustomerAsyncTask;

    /* -----------------------------------------------------------------
     * Launch Helper
     * -----------------------------------------------------------------*/
    public static void launch(Context context, int customerId) {
        context.startActivity(launchIntent(context, customerId));
    }

    private static Intent launchIntent(Context context, int customerId) {
        Class destinationActivity = CustomerDetailActivity.class;
        Intent intent = new Intent(context, destinationActivity);

        Bundle bundle = new Bundle();
        bundle.putInt(CUSTOMER_ID,customerId);
        intent.putExtras(bundle);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_customer_detail);

        // Set toolbar.
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // --------------------------------------
        // Get the Customer Id.
        // --------------------------------------
        Timber.d("... then we get the recipeId from getIntent().getExtras()");
        Intent receivedIntent = getIntent();
        Bundle bundle = receivedIntent.getExtras();
        if (bundle!=null && bundle.containsKey(CUSTOMER_ID))
            mCustomerId = bundle.getInt(CUSTOMER_ID);
        else
            mCustomerId = 0;


        // --------------------------------------
        // Start fragments with the customer id.
        // --------------------------------------
        if(savedInstanceState ==  null) {
            Fragment recipeDetailFragment = CustomerDetailHeaderFragment.newInstance(mCustomerId);

            // Setting the RecipeDetail fragment in the view.
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.customer_detail_header_fragment_holder, recipeDetailFragment).commit();
        }

        // Set the Tab Layout
        mBinding.customerDetailTabLayout.addTab(mBinding.customerDetailTabLayout.newTab().setText("Details"));
        mBinding.customerDetailTabLayout.addTab(mBinding.customerDetailTabLayout.newTab().setText("Visits"));
        mBinding.customerDetailTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Set the View pager
        mPagerAdapter = new CustomerDetailPagerAdapter(getSupportFragmentManager(),mBinding.customerDetailTabLayout.getTabCount(),mCustomerId);
        mBinding.customerDetailViewPager.setAdapter(mPagerAdapter);
        mBinding.customerDetailViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.customerDetailTabLayout));
        mBinding.customerDetailTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.customerDetailViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void showCustomerSelected(int customerId) {
        // No need.
    }

    @Override
    public void deleteCustomer(int customerId) {
        // Cancel any previous asynctask.
        if(mDeleteCustomerAsyncTask!=null && !mDeleteCustomerAsyncTask.isCancelled()) mDeleteCustomerAsyncTask.cancel(true);

        // Initiating an asynctask.
        mDeleteCustomerAsyncTask = new AsyncTask<Object,Void,String>() {
            @Override
            protected String doInBackground(Object[] objects) {

                // Check if the deletion was correct.
                boolean deletionSuccessfully = CustomerDBUtils.deleteCustomerAndVisits(CustomerDetailActivity.this,mCustomerId);

                // If it was correct then I return an empty error message. If not, then i return a "error ocurred" message.
                return deletionSuccessfully ? "" : getString(R.string.an_error_has_ocurred);
            }

            @Override
            protected void onPostExecute(String errorMessage) {
                super.onPostExecute(errorMessage);

                if(errorMessage!= null && errorMessage.isEmpty()) {
                    Toast.makeText(CustomerDetailActivity.this, R.string.customer_deleted_successfully, Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(CustomerDetailActivity.this, R.string.an_error_has_ocurred, Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Execute async task.
        mDeleteCustomerAsyncTask.execute();
    }
}
