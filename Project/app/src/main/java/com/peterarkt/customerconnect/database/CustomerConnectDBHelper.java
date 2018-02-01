package com.peterarkt.customerconnect.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.contracts.VisitContract;
import com.peterarkt.customerconnect.database.utils.CustomerConnectDBUtils;

import timber.log.Timber;

public class CustomerConnectDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "customerconnectdb.db";
    private static final int VERSION = 1;

    public CustomerConnectDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Called when the tasks database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("Creating Database tables...");
        CustomerConnectDBUtils.createTable(db, CustomerContract.CustomerEntry.TABLE_NAME);
        CustomerConnectDBUtils.createTable(db, VisitContract.VisitEntry.TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}