package com.holy.singaporeantaxis;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.holy.singaporeantaxis.helpers.SQLiteHelper;
import com.holy.singaporeantaxis.models.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_SIGN_UP = 100;

    // Edit texts for signing in
    private EditText mIdEdit;
    private EditText mPasswordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize edit texts
        mIdEdit = findViewById(R.id.edit_id);
        mPasswordEdit = findViewById(R.id.edit_password);

        // Set click listeners to views
        Button signInButton = findViewById(R.id.btn_sign_in);
        TextView signUpText = findViewById(R.id.txt_sign_up);
        signInButton.setOnClickListener(this);
        signUpText.setOnClickListener(this);
    }

    // Process View click

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_sign_in:
                // Try signing in
                if (trySigningIn()) {
                    // Start Home Activity
                    startHomeActivity();
                }
                break;
            case R.id.txt_sign_up:
                // Start Sign Up activity
                startSignUpActivity();
                break;
        }
    }

    // Try signing in

    private boolean trySigningIn() {

        // Get signing-in information
        String strId = mIdEdit.getText().toString().trim();
        String strPassword = mPasswordEdit.getText().toString().trim();

        // No empty field allowed
        if (strId.isEmpty() || strPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Process signing in using SQLite Database

        // 1. Check if there is an corresponding id
        User user = SQLiteHelper.getInstance(this).getUser(strId);
        if (user == null) {
            Toast.makeText(this, "ID does not exist", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. Check if the password entered matches with the real one
        if (!strPassword.equals(user.getPassword())) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. Set current id of app
        ((App)getApplication()).setCurrentId(strId);

        return true;
    }

    // Start Home Activity
    private void startHomeActivity() {

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    // Start Sign Up activity

    private void startSignUpActivity() {

        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, REQUEST_SIGN_UP);
    }

    // onActivityResult

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Registration succeeded: Fill in signing in edits with registration data
        if (requestCode == REQUEST_SIGN_UP && resultCode == RESULT_OK && data != null) {

            String id = data.getStringExtra(SignUpActivity.EXTRA_USER_ID);
            String password = data.getStringExtra(SignUpActivity.EXTRA_USER_PASSWORD);
            mIdEdit.setText(id);
            mPasswordEdit.setText(password);
        }
    }
}

