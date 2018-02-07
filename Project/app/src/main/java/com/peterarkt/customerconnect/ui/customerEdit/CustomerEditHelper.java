package com.peterarkt.customerconnect.ui.customerEdit;

import android.content.ContentValues;
import android.content.Context;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;

/**
 * Created by Andrés on 2/6/18.
 */

public class CustomerEditHelper {

    public static String createCustomer(Context context, CustomerEditViewModel customer){

        // Just checking if the context is valid
        if (context == null || customer == null)
            return "An error has ocurred. Please try again";

        // ------------------------------------------------
        // Validate some inputs...
        // ------------------------------------------------

        // Customer name validation
        if(customer.customerName.isEmpty())
            return context.getString(R.string.customer_name_is_required);

        // Customer Address Street validation
        if(customer.customerAddressStreet.isEmpty())
            return context.getString(R.string.customer_address_street_is_required);


        String errorMessage = "";

        try {
            // Insert Customer
            ContentValues cv = new ContentValues();
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME, customer.customerName);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHONE_NUMBER, customer.customerPhoneNumber);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHONE_TYPE, customer.customerPhoneType);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_EMAIL, customer.customerEmail);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_ADDRESS_STREET, customer.customerAddressStreet);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_CITY, customer.customerAddressCity);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_COUNTRY, customer.customerAdressCountry);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LATITUDE, customer.customerAddressLatitude);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LONGITUDE, customer.customerAddressLongitude);
            cv.put(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHOTO_PATH, customer.customerPhotoPath);

            context.getContentResolver().insert(CustomerContract.CustomerEntry.CONTENT_URI, cv);
        }catch (Exception e){
            e.printStackTrace();
            return "An error has ocurred. Please try again.";
        }

        return errorMessage;

    }

}
