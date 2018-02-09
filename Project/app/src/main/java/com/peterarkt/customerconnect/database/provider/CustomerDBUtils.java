package com.peterarkt.customerconnect.database.provider;

import android.content.Context;
import android.database.Cursor;

import com.peterarkt.customerconnect.database.contracts.CustomerContract;

/**
 * Created by Andrés on 2/8/18.
 */

public class CustomerDBUtils {
    public static Cursor getCustomerRecord(Context context, int customerId){
        // If there is a text to be searched, then must be added to the query...
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
}
