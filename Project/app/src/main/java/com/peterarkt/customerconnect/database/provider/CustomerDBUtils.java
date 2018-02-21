package com.peterarkt.customerconnect.database.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.contracts.VisitContract;
import com.peterarkt.customerconnect.ui.customerEdit.CustomerEditViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import timber.log.Timber;


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

    public static Cursor getTodaysVisits(Context context){
        // Get today date at 0:00am.
        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date today = c.getTime(); //the midnight, that's the first second of the day.
        long todayInLong = today.getTime();
        Timber.d("Showing Visits since " + new Date(todayInLong));


        // Get Today's Visits Cursor.
        return context.getContentResolver().query(VisitContract.VisitEntry.CONTENT_URI,
                null,
                VisitContract.VisitEntry.COLUMN_VISIT_DATETIME + " >= ?",
                new String[]{String.valueOf(todayInLong)},
                VisitContract.VisitEntry.COLUMN_VISIT_DATETIME + " DESC LIMIT 10");
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
        String selection = CustomerContract.CustomerEntry._ID + " = ?";
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

    // Method to insert Customer (from the Customer view mdoel in CustomerEditActivity).
    public static boolean deleteCustomerAndVisits(Context context, int customerId){
        // Create the Content Values.
        if(context == null) return false;

        // First delete all the visits for the specified customer.
        int deletedVisitsRows = context.getContentResolver().delete(VisitContract.VisitEntry.CONTENT_URI,
                                            VisitContract.VisitEntry.COLUMN_CUSTOMER_ID + " = ?",
                                            new String[]{String.valueOf(customerId)});
        Timber.d("Deleted Visits: " + deletedVisitsRows);



        // Now delete the Customer.
        int deletedCustomerRows = context.getContentResolver().delete(CustomerContract.CustomerEntry.CONTENT_URI,
                CustomerContract.CustomerEntry._ID + " = ?",
                new String[]{String.valueOf(customerId)});

        return deletedCustomerRows > 0;
    }
}
