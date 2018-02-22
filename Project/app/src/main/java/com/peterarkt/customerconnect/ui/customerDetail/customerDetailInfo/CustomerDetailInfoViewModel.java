package com.peterarkt.customerconnect.ui.customerDetail.customerDetailInfo;


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
