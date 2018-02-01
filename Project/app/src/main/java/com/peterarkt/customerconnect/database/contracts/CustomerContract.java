package com.peterarkt.customerconnect.database.contracts;

import android.net.Uri;
import android.provider.BaseColumns;


public class CustomerContract {

    public static final String CONTENT_AUTHORITY    = "com.peterarkt.customerconnect";
    private static final Uri BASE_CONTENT_URI       = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CUSTOMERS       = "customers";

    public static final class CustomerEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CUSTOMERS).build();


        // TABLE NAME
        public static final String TABLE_NAME = "customer";

        // COLUMNS
        public static final String COLUMN_CUSTOMER_NAME             = "customername";
        public static final String COLUMN_CUSTOMER_PHONE_NUMBER     = "customerphonenumber";
        public static final String COLUMN_CUSTOMER_PHONE_TYPE       = "customerphonetype";
        public static final String COLUMN_CUSTOMER_EMAIL            = "customeremail";
        public static final String COLUMN_CUSTOMER_ADDRESS_STREET   = "customeraddressstreet";
        public static final String COLUMN_CUSTOMER_LATITUDE         = "customeraddresslatitude";
        public static final String COLUMN_CUSTOMER_LONGITUDE        = "customeraddresslongitude";
        public static final String COLUMN_CUSTOMER_CITY             = "customercity";
        public static final String COLUMN_CUSTOMER_COUNTRY          = "customercountry";
        public static final String COLUMN_CUSTOMER_PHOTO_PATH       = "customerphotopath";
    }
}