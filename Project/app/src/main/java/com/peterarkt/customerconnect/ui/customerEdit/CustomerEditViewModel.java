package com.peterarkt.customerconnect.ui.customerEdit;

/**
 * Created by Andrés on 2/6/18.
 */

public class CustomerEditViewModel {
    int customerId;
    String customerName;
    String customerPhoneNumber;
    String customerPhoneType;
    String customerEmail;
    String customerAddressStreet;
    String customerAddressCity;
    String customerAdressCountry;
    double customerAddressLatitude;
    double customerAddressLongitude;
    String customerPhotoPath;

    CustomerEditViewModel(){
        this.customerId                  = 0;
        this.customerName                = "";
        this.customerPhoneNumber         = "";
        this.customerPhoneType           = "";
        this.customerEmail               = "";
        this.customerAddressStreet       = "";
        this.customerAddressCity         = "";
        this.customerAdressCountry       = "";
        this.customerAddressLatitude     = 0.00;
        this.customerAddressLongitude    = 0.00;
        this.customerPhotoPath           = "";
    }

}
