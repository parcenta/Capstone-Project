package com.peterarkt.customerconnect.ui.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by USUARIO on 13/02/2018.
 */

public class MediaUtils {

    // Source: https://developer.android.com/training/camera/photobasics.html
    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHmmss").format(new Date());
        String imageFileName = "IMG" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,
                                        ".jpg",
                                        storageDir);
        return image;
    }

}
