package com.peterarkt.customerconnect.ui;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.contracts.VisitContract;
import com.peterarkt.customerconnect.databinding.ActivityCustomerConnectMainBinding;
import com.peterarkt.customerconnect.ui.customerDetail.CustomerDetailActivity;

public class CustomerConnectMainActivity extends AppCompatActivity implements CustomerConnectMainActivityHandler{

    ActivityCustomerConnectMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_customer_connect_main);



    }

    @Override
    public void showCustomerSelected(int customerId) {
        CustomerDetailActivity.launch(this,customerId);
    }
}
