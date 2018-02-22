package com.peterarkt.customerconnect.ui.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaUtils {

    /* ---------------------------------------------------------------------------------
    * Methods to attach photos
    * Source: https://stackoverflow.com/questions/18220152/opening-an-image-using-intent-action-pick
    * ---------------------------------------------------------------------------------*/
    public static void launchAttachImageForGallery(Activity activity, int readExternalStoragePermissionCode, int activityResultEventCode){
        // First check permission to access local files
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, readExternalStoragePermissionCode);
            return;
        }

        // Launch picker.
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, activityResultEventCode);
    }

    public static String getAttachedImagePath(Context context, Intent data){
        if(data == null) return "";

        String imagePath = "";
        try {
            Uri selectedImageURI = data.getData();
            imagePath = getRealPathFromURI(context,selectedImageURI);
        }catch (Exception e){
            e.printStackTrace();
        }

        return imagePath;
    }


    // Source: https://stackoverflow.com/questions/42344154/how-to-get-path-of-selected-image-from-gallery
    @SuppressLint("NewApi")
    private static String getRealPathFromURI(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        if(cursor != null){
            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }

        return filePath;
    }


    /* ---------------------------------------------------------------------------------
    * Methods to create file (used in
    * ---------------------------------------------------------------------------------*/
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
