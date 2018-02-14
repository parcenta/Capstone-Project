package com.peterarkt.customerconnect.ui.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by USUARIO on 13/02/2018.
 */

public class PermissionUtils {

    public static boolean permissionIsGranted(Context context,String permission){
        return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }

}
