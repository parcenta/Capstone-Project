package com.peterarkt.customerconnect.database.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.contracts.VisitContract;

/**
 * Created by Andrés on 2/8/18.
 */

public class CustomerDBUtils {
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
}
