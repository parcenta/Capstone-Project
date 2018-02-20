package com.peterarkt.customerconnect.ui.utils;

/**
 * Created by USUARIO on 19/02/2018.
 */

public class ValidationUtils {

    // Source: https://stackoverflow.com/questions/15111420/how-to-check-if-a-string-contains-only-digits-in-java
    public static boolean phoneNumberIsValid(String phoneNumber){
        String validRegex = "\\d+";
        return phoneNumber.matches(validRegex);
    }
}
