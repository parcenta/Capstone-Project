package com.peterarkt.customerconnect.database.contracts;

import android.net.Uri;
import android.provider.BaseColumns;


public class VisitContract {

    public static final String CONTENT_AUTHORITY    = "com.peterarkt.customerconnect";
    private static final Uri BASE_CONTENT_URI       = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_VISITS          = "visits";

    public static final class VisitEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VISITS).build();


        // TABLE NAME
        public static final String TABLE_NAME = "visit";

        // COLUMNS
        public static final String COLUMN_CUSTOMER_ID               = "customerid";
        public static final String COLUMN_VISIT_DATETIME            = "visitdatetime";
        public static final String COLUMN_VISIT_COMMENTARY          = "visitcommentary";
    }
}