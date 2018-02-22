package com.peterarkt.customerconnect.ui.customersList;


public class CustomerItem {

    public int customerId;
    public String customerName;
    public String customerAddressStreet;
    public String customerPhotoUrl;

    public CustomerItem(int customerId, String customerName, String customerAddressStreet, String customerPhotoUrl) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerAddressStreet = customerAddressStreet;
        this.customerPhotoUrl = customerPhotoUrl;
    }
}
