package com.peterarkt.customerconnect.ui.customerEdit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.database.contracts.CustomerContract;
import com.peterarkt.customerconnect.database.provider.CustomerDBUtils;
import com.peterarkt.customerconnect.ui.utils.Constants;
import com.peterarkt.customerconnect.ui.utils.ValidationUtils;

import org.json.JSONArray;
import org.json.JSONObject;


public class CustomerEditHelper {

    // Method that returns an error message if something goes wrong. If everything is fine, then it will return an empty message.
    public static String createOrUpdateCustomer(Context context, CustomerEditViewModel customer, String editMode, int customerIdToUpdate){

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

        // Customer Address email
        if(customer.customerEmail.length() > 0 && !ValidationUtils.emailIsValid(customer.customerEmail))
            return context.getString(R.string.email_is_not_valid);

        // Validate Phone number. Must be only numbers.
        if(customer.customerPhoneNumber.length() > 0 && !ValidationUtils.phoneNumberIsValid(customer.customerPhoneNumber))
            return context.getString(R.string.phone_number_is_not_valid);


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

            switch (editMode) {
                case Constants.INSERT_MODE:
                    boolean insertedSuccessfully = CustomerDBUtils.insertCustomer(context,cv);
                    return insertedSuccessfully ? "" : context.getString(R.string.an_error_has_ocurred);

                case Constants.UPDATE_MODE:
                    boolean updatedSuccessfully = CustomerDBUtils.updateCustomer(context,customerIdToUpdate,cv);
                    return updatedSuccessfully ? "" : context.getString(R.string.an_error_has_ocurred);

                default:
                    return context.getString(R.string.an_error_has_ocurred);
            }
        }catch (Exception e){
            e.printStackTrace();
            return "An error has ocurred. Please try again.";
        }
    }

    public static CustomerEditViewModel getCustomerRecordAsViewModel(Context context, int customerId){
        CustomerEditViewModel viewModelFromDB = new CustomerEditViewModel();

        Cursor cursor = CustomerDBUtils.getCustomerRecord(context,customerId);
        if(cursor != null){
            try {
                // Get the values from the cursor
                if(cursor.moveToFirst()){
                    viewModelFromDB.customerId                  = cursor.getInt(cursor.getColumnIndex(CustomerContract.CustomerEntry._ID));
                    viewModelFromDB.customerName                = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_NAME));
                    viewModelFromDB.customerPhoneNumber         = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHONE_NUMBER));
                    viewModelFromDB.customerPhoneType           = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHONE_TYPE));
                    viewModelFromDB.customerEmail               = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_EMAIL));
                    viewModelFromDB.customerAddressStreet       = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_ADDRESS_STREET));
                    viewModelFromDB.customerAddressCity         = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_CITY));
                    viewModelFromDB.customerAdressCountry       = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_COUNTRY));
                    viewModelFromDB.customerAddressLatitude     = cursor.getDouble(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LATITUDE));
                    viewModelFromDB.customerAddressLongitude    = cursor.getDouble(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_LONGITUDE));
                    viewModelFromDB.customerPhotoPath           = cursor.getString(cursor.getColumnIndex(CustomerContract.CustomerEntry.COLUMN_CUSTOMER_PHOTO_PATH));
                }
            }catch (Exception e){
                e.printStackTrace();
                if(!cursor.isClosed()) cursor.close();
                return null;
            }finally {
                // Closing the cursor.
                if(!cursor.isClosed()) cursor.close();
            }

        }

        return viewModelFromDB;
    }


    public static String getCityAndCountryFromJson(String jsonResponse) throws Exception {

        String cityString = "";
        String countryString = "";

        // Convert the JsonString to JsonObject.
        JSONObject movieDetailJson = new JSONObject(jsonResponse);

        // Get Values from JSON.
        JSONArray resultsArray = movieDetailJson.getJSONArray("results");
        if(resultsArray == null || resultsArray.length() == 0)
            return null;

            // Get First result as JsonObject.
            JSONObject oneResult = resultsArray.getJSONObject(0);
            if(oneResult == null) return null;

            // Get "address_components" JsonArray from the JsonObject.
            JSONArray componentsArray = oneResult.getJSONArray("address_components");
            if(componentsArray == null || componentsArray.length() == 0)
                return null;

            // Loop components.
            for(int j=0; j < componentsArray.length(); j++){

                // Get one result as JsonObject.
                JSONObject oneComponent = componentsArray.getJSONObject(j);
                if(oneComponent == null) return null;

                // First get the types. We need to search for "locality" for city and "country" for country.
                JSONArray typesArray = oneComponent.getJSONArray("types");

                boolean isCity      = false;
                boolean isCountry   = false;
                for(int a=0;a<typesArray.length();a++){
                    String oneType = typesArray.getString(a);
                    if(oneType.equals("locality")) isCity = true;
                    else if (oneType.equals("country")) isCountry = true;
                }

                if(isCity) cityString = oneComponent.getString("short_name");
                if(isCountry) countryString = oneComponent.getString("short_name");

            }


        if(cityString.isEmpty() && countryString.isEmpty())
            return "";
        else
            return cityString + "," + countryString;

    }

}
