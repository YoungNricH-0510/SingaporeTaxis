package com.holy.singaporeantaxis.models;

import com.google.android.gms.maps.model.LatLng;

public class User {

    private final String id;
    private final String password;
    private final String phone;
    private final boolean isMale;
    private final boolean isSignedIn;
    private final LatLng lastLocation;

    public User(String id, String password, String phone, boolean isMale,
                boolean isSignedIn, LatLng lastLocation) {
        this.id = id;
        this.password = password;
        this.phone = phone;
        this.isMale = isMale;
        this.isSignedIn = isSignedIn;
        this.lastLocation = lastLocation;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isMale() {
        return isMale;
    }

    public boolean isSignedIn() {
        return isSignedIn;
    }

    public LatLng getLastLocation() {
        return lastLocation;
    }
}
