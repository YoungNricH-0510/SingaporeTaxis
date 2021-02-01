package com.holy.singaporeantaxis;

import android.app.Application;

import com.holy.singaporeantaxis.helpers.SQLiteHelper;

import java.util.Random;

public class App extends Application {

    // user id of currently logged in
    private String currentId = null;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public void setCurrentId(String id) {
        currentId = id;
    }

    public String getCurrentId() {
        return currentId;
    }

}
