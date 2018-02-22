package com.peterarkt.customerconnect.ui.utils;

import android.content.Context;

import com.peterarkt.customerconnect.R;

public class Constants {

    public final static String UPDATE_MODE = "UPD";
    public final static String INSERT_MODE = "INS";

    public static String getAddressTypeDescription(Context context, int position){
        if(context == null) return "ERROR";

        switch (position){
            case 0:
                return context.getString(R.string.home_type);
            case 1:
                return context.getString(R.string.work_type);
            case 2:
                return context.getString(R.string.other_type);
            default:
                return "N/A";
        }
    }

}
