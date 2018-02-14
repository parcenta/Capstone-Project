package com.peterarkt.customerconnect.ui.customerEdit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrés on 2/6/18.
 */

public class CustomerEditViewModel implements Parcelable{
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
    boolean photoReadyToLoad;

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



    protected CustomerEditViewModel(Parcel in) {
        customerId = in.readInt();
        customerName = in.readString();
        customerPhoneNumber = in.readString();
        customerPhoneType = in.readString();
        customerEmail = in.readString();
        customerAddressStreet = in.readString();
        customerAddressCity = in.readString();
        customerAdressCountry = in.readString();
        customerAddressLatitude = in.readDouble();
        customerAddressLongitude = in.readDouble();
        customerPhotoPath = in.readString();
        photoReadyToLoad = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(customerId);
        dest.writeString(customerName);
        dest.writeString(customerPhoneNumber);
        dest.writeString(customerPhoneType);
        dest.writeString(customerEmail);
        dest.writeString(customerAddressStreet);
        dest.writeString(customerAddressCity);
        dest.writeString(customerAdressCountry);
        dest.writeDouble(customerAddressLatitude);
        dest.writeDouble(customerAddressLongitude);
        dest.writeString(customerPhotoPath);
        dest.writeByte((byte) (photoReadyToLoad ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CustomerEditViewModel> CREATOR = new Parcelable.Creator<CustomerEditViewModel>() {
        @Override
        public CustomerEditViewModel createFromParcel(Parcel in) {
            return new CustomerEditViewModel(in);
        }

        @Override
        public CustomerEditViewModel[] newArray(int size) {
            return new CustomerEditViewModel[size];
        }
    };

}
