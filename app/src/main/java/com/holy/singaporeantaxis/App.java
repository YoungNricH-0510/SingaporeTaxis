package com.holy.singaporeantaxis;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.holy.singaporeantaxis.helpers.SQLiteHelper;
import com.holy.singaporeantaxis.models.User;

import java.io.IOException;
import java.util.Random;

public class App extends Application {

    // user id of currently logged in
    private String currentId;

    @Override
    public void onCreate() {
        super.onCreate();

        currentId = null;

        // Create dummy users into DB
        if (SQLiteHelper.getInstance(this).getAllUsers().isEmpty()) {
            createDummyUsers();
        }
    }

    private void createDummyUsers() {

        // Names of locations where dummies are to be located
        String[] locationNames = {
                "Hong Lim Park",
                "OCBC Centre",
                "Hotel Swissôtel Merchant Court",
                "Koji Sushi Bar - Pickering Branch",
                "Chinatown heritage centre",
                "JUMBO seafood the riverwalk",
                "Yoga movement circular Rd",
        };

        // Get GPS Locations from the location names
        LatLng[] latLngs = new LatLng[locationNames.length];
        Geocoder geocoder = new Geocoder(this);
        for (int i = 0; i < locationNames.length; i++) {
            try {
                Address address = geocoder.getFromLocationName(locationNames[i], 1).get(0);
                latLngs[i] = new LatLng(address.getLatitude(), address.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        User[] dummies = {
                new User("tester", "1234", "0101234567", true,
                        true, latLngs[0]),
                new User("Mary", "3000", "4031234567", false,
                        true, latLngs[1]),
                new User("Mariah", "4000", "2701234567", false,
                        true, latLngs[2]),
                new User("Jameson", "5000", "2251234567", true,
                        true, latLngs[3]),
                new User("Jackson", "6000", "7901234567", true,
                        true, latLngs[4]),
                new User("Morris", "7000", "3541234567", true,
                        true, latLngs[5]),
                new User("Anne", "8000", "1011234567", false,
                        true, latLngs[6]),
        };

        for (User dummy : dummies) {
            SQLiteHelper.getInstance(this).addUser(dummy);
        }
    }

    public void setCurrentId(String id) {
        currentId = id;
    }

    public String getCurrentId() {
        return currentId;
    }

}
