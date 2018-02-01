package com.peterarkt.customerconnect.database.utils;

import android.database.sqlite.SQLiteDatabase;

import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.contracts.VisitContract;

import timber.log.Timber;

public class CustomerConnectDBUtils {
    public static void createTable(SQLiteDatabase db, String tableName){

        String CREATE_TABLE_SQL = null;

        switch (tableName){
            case CustomerContract.CustomerEntry.TABLE_NAME:
                Timber.d("Creating Customer table...");
                CREATE_TABLE_SQL = "CREATE TABLE "  + CustomerContract.CustomerEntry.TABLE_NAME + " (" +
                        CustomerContract.CustomerEntry._ID                              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME             + " TEXT NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHONE_NUMBER     + " TEXT NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHONE_TYPE       + " TEXT NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_EMAIL            + " TEXT NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_ADDRESS_STREET   + " TEXT NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_CITY             + " TEXT NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_COUNTRY          + " TEXT NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LATITUDE         + " REAL NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LONGITUDE        + " REAL NOT NULL, " +
                        CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHOTO_PATH       + " TEXT NOT NULL); ";
                break;
            case VisitContract.VisitEntry.TABLE_NAME:
                Timber.d("Creating Visit table...");
                CREATE_TABLE_SQL = "CREATE TABLE "  + VisitContract.VisitEntry.TABLE_NAME + " (" +
                        VisitContract.VisitEntry._ID                      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        VisitContract.VisitEntry.COLUMN_CUSTOMER_ID       + " INTEGER NOT NULL, " +
                        VisitContract.VisitEntry.COLUMN_VISIT_DATETIME    + " DATETIME NOT NULL, " +
                        VisitContract.VisitEntry.COLUMN_VISIT_COMMENTARY  + " TEXT NOT NULL); ";
                break;
        }

        if (CREATE_TABLE_SQL != null)
            db.execSQL(CREATE_TABLE_SQL);
    }
}
