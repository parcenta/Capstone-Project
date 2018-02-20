package com.peterarkt.customerconnect.ui;


import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.databinding.ActivityCustomerConnectMainBinding;
import com.peterarkt.customerconnect.ui.customerDetail.CustomerDetailActivity;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailHeader.CustomerDetailHeaderFragment;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailInfo.CustomerDetailInfoFragment;
import com.peterarkt.customerconnect.ui.customerDetail.customerDetailVisits.CustomerVisitsFragment;

public class CustomerConnectMainActivity extends AppCompatActivity implements CustomerConnectMainActivityHandler{

    ActivityCustomerConnectMainBinding mBinding;

    boolean isTablet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binding the view.
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_customer_connect_main);

        // Set toolbar
        setSupportActionBar(mBinding.toolbar);

        // Detect if it is a tablet
        isTablet = getResources().getBoolean(R.bool.isTablet);

    }

    @Override
    public void showCustomerSelected(int customerId) {

        // Detect if fragment containers are present (is equivalent to ask if it is a tablet).
        boolean hasFragmentContainer = findViewById(R.id.customer_detail_container) != null;

        // If is Tablet (sw600dp)
        if(hasFragmentContainer){
            FragmentManager fm = getSupportFragmentManager();

            // Create the fragments for the Customer Detail.
            CustomerDetailHeaderFragment customerDetailHeaderFragment = CustomerDetailHeaderFragment.newInstance(customerId);
            CustomerDetailInfoFragment customerDetailInfoFragment = CustomerDetailInfoFragment.newInstance(customerId);
            CustomerVisitsFragment customerDetailVisitsFragment = CustomerVisitsFragment.newInstance(customerId);

            // Now replace them in their respective places.
            fm.beginTransaction().replace(R.id.customer_detail_header_fragment_holder, customerDetailHeaderFragment)
                                .replace(R.id.customer_detail_info_fragment_holder,customerDetailInfoFragment)
                                .replace(R.id.customer_detail_visits_fragment_holder,customerDetailVisitsFragment).commit();
        }else{
            CustomerDetailActivity.launch(this,customerId);
        }
    }

    @Override
    public void deleteCustomer(final int customerId) {
        /*new AsyncTask<Void,Void,Boolean>(this){

            @Override
            protected Boolean doInBackground(Void... voids) {
                return CustomerDBUtils.deleteCustomerAndVisits(getApplicationContext(),customerId);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {

            }
        }.execute();*/
    }
}
