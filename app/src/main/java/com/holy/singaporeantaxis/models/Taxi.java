package com.holy.singaporeantaxis.models;

import com.google.android.gms.maps.model.LatLng;

public class Taxi {

    private final LatLng location;

    public Taxi(double latitude, double longitude) {
        this.location = new LatLng(latitude, longitude);
    }

    public LatLng getLocation() {
        return location;
    }

}
