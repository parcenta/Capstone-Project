package com.peterarkt.customerconnect.ui.customerDetail.customerDetailInfo;

/**
 * Created by Andrés on 2/9/18.
 */

public class CustomerDetailInfoViewModel {

    public String customerPhoneNumber;
    public String customerPhoneType;
    public String customerEmail;
    public String customerAddressStreet;
    public String customerCity;
    public String customerCountry;
    public double customerAddressLatitude;
    public double customerAddressLongitude;

    public CustomerDetailInfoViewModel(){
        customerPhoneNumber     = "";
        customerPhoneType       = "";
        customerEmail           = "";
        customerAddressStreet   = "";
        customerCity            = "";
        customerCountry         = "";
        customerAddressLatitude = 0.00;
        customerAddressLongitude= 0.00;
    }

}
