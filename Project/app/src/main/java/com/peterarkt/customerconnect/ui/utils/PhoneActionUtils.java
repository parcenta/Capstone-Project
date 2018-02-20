package com.peterarkt.customerconnect.ui.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.peterarkt.customerconnect.R;

/**
 * Created by USUARIO on 19/02/2018.
 */

public class PhoneActionUtils {

    public static void openImage(Context context, String photoPath){
        if(context == null) return;

        if(photoPath.isEmpty()){
            Toast.makeText(context,"No image is found.",Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + photoPath), "image/*");
        context.startActivity(intent);
    }


    public static void makePhoneCall(Context context, String phoneNumber){
        if(context == null || phoneNumber == null) return;

        if(phoneNumber.isEmpty() || !ValidationUtils.phoneNumberIsValid(phoneNumber)){
            Toast.makeText(context,context.getString(R.string.phone_not_valid_for_phone_call),Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" +phoneNumber));
        context.startActivity(intent);
    }

    // Source: https://stackoverflow.com/questions/8701634/send-email-intent
    public static void sendEmail(Context context, String email){
        if(context == null) return;

        if(email.isEmpty() || email.equalsIgnoreCase("Unknown")) {
            Toast.makeText(context, context.getString(R.string.email_not_valid_for_send_email), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, email);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        intent.putExtra(Intent.EXTRA_TEXT, "Hi from CustomerConnect.");
        intent.setType("text/plain");

        context.startActivity(Intent.createChooser(intent, "Send Email"));
    }

    // Source: https://developers.google.com/maps/documentation/urls/android-intents
    public static void navigateToCoordinates(Context context, double latitude, double longitude){
        if(context == null) return;

        if(latitude == 0.00 && longitude == 0.00) {
            Toast.makeText(context, context.getString(R.string.no_coordinates_for_navigate), Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }

}
