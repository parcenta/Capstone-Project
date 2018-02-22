package com.peterarkt.customerconnect.ui.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;


public class PermissionUtils {

    public static boolean permissionIsGranted(Context context,String permission){
        return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }

}
