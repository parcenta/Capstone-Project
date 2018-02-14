package com.peterarkt.customerconnect.ui.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by USUARIO on 13/02/2018.
 */

public class CustomerConnectUIUtils {

    private static final String MMMddYYYYFormat = "MMM dd, yyyy";

    // Formatting the Date
    public static String getDateAsMMMddYYYY(Date date){
        DateFormat df = new SimpleDateFormat(MMMddYYYYFormat);
        return df.format(date);
    }
}
