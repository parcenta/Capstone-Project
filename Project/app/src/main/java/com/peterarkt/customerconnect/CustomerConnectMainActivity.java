package com.peterarkt.customerconnect;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.contracts.VisitContract;

public class CustomerConnectMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_connect_main);


        // TODO: Just to check if the provider are working and the tables created correctly. Must delete this code.
        Cursor cursor = getContentResolver().query(CustomerContract.CustomerEntry.CONTENT_URI,null,null,null,null,null);
        if(cursor!= null) cursor.close();
        cursor = null;

        cursor = getContentResolver().query(VisitContract.VisitEntry.CONTENT_URI,null,null,null,null,null);
        if(cursor!= null) cursor.close();
    }
}
