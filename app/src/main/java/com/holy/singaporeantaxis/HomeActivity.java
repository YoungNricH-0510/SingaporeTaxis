package com.holy.singaporeantaxis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;

public class HomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Insert maps fragment first
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frag_container, new MapsFragment())
                .commit();
    }

    // onBackPressed

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to sign out?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                    // Sign out
                    ((App)getApplication()).setCurrentId(null);
                    super.onBackPressed();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }



}