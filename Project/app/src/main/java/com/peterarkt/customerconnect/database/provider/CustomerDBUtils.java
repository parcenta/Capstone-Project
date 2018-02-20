package com.peterarkt.customerconnect.database.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.contracts.VisitContract;
import com.peterarkt.customerconnect.ui.customerEdit.CustomerEditViewModel;


public class CustomerDBUtils {

    // Method to get a single customer record (by id)
    public static Cursor getCustomerRecord(Context context, int customerId){

        String selection = CustomerContract.CustomerEntry._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(customerId)};

        // Search for the Customer.
        Cursor cursor = context.getContentResolver().query(CustomerContract.CustomerEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);

        return cursor;
    }


    // Method that returns the visits of a single customer (by customerId). Can be ordered by.
    public static Cursor getCustomerVisitsListRecord(Context context, int customerId, String orderBy){

        String selection = VisitContract.VisitEntry.COLUMN_CUSTOMER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(customerId)};

        // Search for the Customer's visits.
        Cursor cursor = context.getContentResolver().query(VisitContract.VisitEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                orderBy);

        return cursor;
    }


    // Method to insert new visit for a single customer.
    public static boolean insertNewCustomerVisit(Context context, int customerId, long visitDateLong, String visitCommentary){
        // Create the Content Values.
        ContentValues cv = new ContentValues();
        cv.put(VisitContract.VisitEntry.COLUMN_CUSTOMER_ID,customerId);
        cv.put(VisitContract.VisitEntry.COLUMN_VISIT_DATETIME,visitDateLong);
        cv.put(VisitContract.VisitEntry.COLUMN_VISIT_COMMENTARY,visitCommentary);

        // Search for the Customer.
        Uri insertUri = context.getContentResolver().insert(VisitContract.VisitEntry.CONTENT_URI,cv);
        long newId = ContentUris.parseId(insertUri);
        return newId > 0;
    }

    // Method to update a Customer (from the Customer view mdoel in CustomerEditActivity).
    public static boolean updateCustomer(Context context, int customerId, ContentValues cv){
        // Create the Content Values.
        if(cv == null || context == null) return false;

        // Set the Selection.
        String selection = VisitContract.VisitEntry.COLUMN_CUSTOMER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(customerId)};

        // Update the Customer.
        int updatedRows = context.getContentResolver().update(CustomerContract.CustomerEntry.CONTENT_URI,cv,selection,selectionArgs);
        return updatedRows > 0;
    }

    // Method to insert Customer (from the Customer view mdoel in CustomerEditActivity).
    public static boolean insertCustomer(Context context, ContentValues cv){
        // Create the Content Values.
        if(cv == null || context == null) return false;

        // Insert new Customer.
        Uri insertUri = context.getContentResolver().insert(CustomerContract.CustomerEntry.CONTENT_URI,cv);
        long newId = ContentUris.parseId(insertUri);
        return newId > 0;
    }
}
