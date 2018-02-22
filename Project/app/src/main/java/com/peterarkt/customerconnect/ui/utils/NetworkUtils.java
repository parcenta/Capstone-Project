package com.peterarkt.customerconnect.ui.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.peterarkt.customerconnect.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import timber.log.Timber;


public class NetworkUtils {


    // Api to search vity and country with given coordinats
    // Example: https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY

    // Base URLs
    private static final String GOOGLE_GEOCODE_API_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    // Query Parameters
    private static final String API_KEY_QUERY = "key";
    private static final String LATITUDE_AND_LONGITUDE_QUERY = "latlng";



    public static URL buildUrlForSearchCityAndCountry(Context context, double latitude, double longitude) {


        // Began to build the Uri with base url.
        Uri builtUri = Uri.parse(GOOGLE_GEOCODE_API_BASE_URL).buildUpon()
                .appendQueryParameter(LATITUDE_AND_LONGITUDE_QUERY,latitude+","+longitude)
                .appendQueryParameter(API_KEY_QUERY, context.getString(R.string.google_maps_api_key_for_webservice))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Timber.d("Built URI " + url);

        return url;
    }



    /**
     * This method returns the entire result from the HTTP response.
     * Source:  Udacity Sunshine Project
     */
    public static String getResponseFromHttpUrl(Context context, URL url) throws Exception {

        if(!isOnline(context))
            return null;

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    // Taken from StackOverflow post. https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
    private static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null) return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
