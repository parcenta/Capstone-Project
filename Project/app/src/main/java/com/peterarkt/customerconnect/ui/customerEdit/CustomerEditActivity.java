package com.peterarkt.customerconnect.ui.customerEdit;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.peterarkt.customerconnect.R;
import com.peterarkt.customerconnect.databinding.ActivityCustomerEditBinding;

public class CustomerEditActivity extends AppCompatActivity  implements OnMapReadyCallback {

    ActivityCustomerEditBinding mBinding;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_customer_edit);


        mBinding.locationLayout.inputCustomerAddressCity.setText("Hola");


        // Set the Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng coordinates = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(coordinates).title("Current Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
    }
}
