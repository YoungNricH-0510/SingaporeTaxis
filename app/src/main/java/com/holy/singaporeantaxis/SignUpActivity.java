package com.holy.singaporeantaxis;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.holy.singaporeantaxis.helpers.SQLiteHelper;
import com.holy.singaporeantaxis.models.User;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_USER_ID = "com.holy.userId";
    public static final String EXTRA_USER_PASSWORD = "com.holy.userPassword";

    // Widgets for signing up
    private EditText mIdEdit;
    private EditText mPasswordEdit;
    private EditText mPhoneEdit;
    private RadioGroup mGenderRadio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        mIdEdit = findViewById(R.id.edit_id);
        mPasswordEdit = findViewById(R.id.edit_password);
        mPhoneEdit = findViewById(R.id.edit_phone);
        mGenderRadio = findViewById(R.id.radio_gender);

        // Set view click listeners
        Button signUpButton = findViewById(R.id.btn_sign_up);
        TextView signInText = findViewById(R.id.txt_sign_in);
        signUpButton.setOnClickListener(this);
        signInText.setOnClickListener(this);
    }

    // Process view click

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_sign_up:
                // Try signing up
                if (trySigningUp()) {
                    // Finish activity with registration data
                    finishActivityWithRegistrationData();
                }
                break;
            case R.id.txt_sign_in:
                // Finish activity with result CANCELED
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    // Try signing up

    private boolean trySigningUp() {

        // Get signing-up information
        String id = mIdEdit.getText().toString().trim();
        String password = mPasswordEdit.getText().toString().trim();
        String phone = mPhoneEdit.getText().toString().trim();
        int genderButtonId = mGenderRadio.getCheckedRadioButtonId();

        // Validate inputs
        if (id.length() < 4) {
            Toast.makeText(this,
                    "ID must be 4 chars or longer", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 4) {
            Toast.makeText(this,
                    "Password must be 4 chars or longer", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (phone.length() < 8) {
            Toast.makeText(this,
                    "Phone number must be 8 chars or longer", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (genderButtonId == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Process registration using SQLite DB

        // 1. Check if there is an existing user
        User existingUser = SQLiteHelper.getInstance(this).getUser(id);
        if (existingUser != null) {
            Toast.makeText(this, "ID already exists", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. Insert user to DB
        boolean isMale = (genderButtonId == R.id.btn_male);
        User user = new User(id, password, phone, isMale);
        SQLiteHelper.getInstance(this).addUser(user);

        // Show success message
        Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();

        return true;
    }

    // Finish activity with registration data

    private void finishActivityWithRegistrationData() {

        // Get signing-up information
        String id = mIdEdit.getText().toString().trim();
        String password = mPasswordEdit.getText().toString().trim();

        Intent data = new Intent();
        data.putExtra(EXTRA_USER_ID, id);
        data.putExtra(EXTRA_USER_PASSWORD, password);

        setResult(RESULT_OK, data);
        finish();
    }

}