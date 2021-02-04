package com.holy.singaporeantaxis.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.holy.singaporeantaxis.models.User;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SQLiteHelper extends SQLiteOpenHelper {

    // Database information
    public static final String DATABASE_NAME = "db";
    public static final int DATABASE_VERSION = 11;

    // User table information
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_IS_MALE = "isMale";
    public static final String COLUMN_USER_IS_SIGNED_IN = "isSignedIn";
    public static final String COLUMN_USER_LAST_LATITUDE = "lastLatitude";
    public static final String COLUMN_USER_LAST_LONGITUDE = "lastLongitude";


    // Database helper instance
    private static SQLiteHelper instance;

    // Get instance of database helper
    public static synchronized SQLiteHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Constructor
    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create user table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS
                + "("
                + COLUMN_USER_ID + " TEXT PRIMARY KEY, "
                + COLUMN_USER_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_USER_PHONE + " TEXT NOT NULL, "
                + COLUMN_USER_IS_MALE + " INTEGER NOT NULL, "
                + COLUMN_USER_IS_SIGNED_IN + " INTEGER NOT NULL, "
                + COLUMN_USER_LAST_LATITUDE + " REAL, "
                + COLUMN_USER_LAST_LONGITUDE + " REAL"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Create new table
        if (newVersion != oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }


    // Add new user
    public void addUser(User user) {

        // Open writable database
        SQLiteDatabase db = getWritableDatabase();

        // DB 입력 시작
        db.beginTransaction();
        try {
            // 유저 정보를 모두 values 객체에 입력한다
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, user.getId());
            values.put(COLUMN_USER_PASSWORD, user.getPassword());
            values.put(COLUMN_USER_PHONE, user.getPhone());
            values.put(COLUMN_USER_IS_MALE, user.isMale() ? 1 : 0);
            values.put(COLUMN_USER_IS_SIGNED_IN, user.isSignedIn() ? 1 : 0);
            if (user.getLastLocation() != null) {
                values.put(COLUMN_USER_LAST_LATITUDE, user.getLastLocation().latitude);
                values.put(COLUMN_USER_LAST_LONGITUDE, user.getLastLocation().longitude);
            }

            // 데이터베이스에 values 를 입력한다.
            db.insertOrThrow(TABLE_USERS, null, values);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    // Update signed state of the specified user
    public void updateUserSignedState(String id, boolean isSignedIn) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_IS_SIGNED_IN, isSignedIn ? 1 : 0);

        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[] { id });
    }

    // Update last location of the specified user
    public void updateUserLastLocation(String id, LatLng lastLocation) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_LAST_LATITUDE, lastLocation.latitude);
        values.put(COLUMN_USER_LAST_LONGITUDE, lastLocation.longitude);

        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[] { id });
    }

    // Get user who has the specified id
    public User getUser(String id) {

        User user = null;

        // 읽기용 DB 를 연다.
        SQLiteDatabase db = getReadableDatabase();

        // 지정된 id 를 갖는 유저 데이터를 가리키는 커서를 검색한다.
        String SELECT_USER =
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USER_ID + " = '" + id + "'";

        Cursor cursor = db.rawQuery(SELECT_USER, null);

        try {
            if (cursor.moveToFirst()) {
                // 커서로부터 유저 데이터를 가져온다.
                String password = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD));
                String phone = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PHONE));
                boolean isMale = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_MALE)) == 1);
                boolean isSignedIn = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_SIGNED_IN)) == 1);

                LatLng lastLocation = null;
                int columnLastLatitude = cursor.getColumnIndex(COLUMN_USER_LAST_LATITUDE);
                int columnLastLongitude = cursor.getColumnIndex(COLUMN_USER_LAST_LONGITUDE);
                if (!cursor.isNull(columnLastLatitude) && !cursor.isNull(columnLastLongitude)) {
                    double lastLatitude = cursor.getDouble(columnLastLatitude);
                    double lastLongitude = cursor.getDouble(columnLastLongitude);
                    lastLocation = new LatLng(lastLatitude, lastLongitude);
                }

                // 유저 데이터로 유저 객체를 만들어 리턴한다.
                user = new User(id, password, phone, isMale, isSignedIn, lastLocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return user;
    }

    // Get user who has the specified phone number
    public User getUserBy(String phone) {

        User user = null;

        // 읽기용 DB 를 연다.
        SQLiteDatabase db = getReadableDatabase();

        // 지정된 id 를 갖는 유저 데이터를 가리키는 커서를 검색한다.
        String SELECT_USER =
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USER_PHONE + " = '" + phone + "'";

        Cursor cursor = db.rawQuery(SELECT_USER, null);

        try {
            if (cursor.moveToFirst()) {
                // 커서로부터 유저 데이터를 가져온다.
                String id = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                String password = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD));
                boolean isMale = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_MALE)) == 1);
                boolean isSignedIn = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_SIGNED_IN)) == 1);

                LatLng lastLocation = null;
                int columnLastLatitude = cursor.getColumnIndex(COLUMN_USER_LAST_LATITUDE);
                int columnLastLongitude = cursor.getColumnIndex(COLUMN_USER_LAST_LONGITUDE);
                if (!cursor.isNull(columnLastLatitude) && !cursor.isNull(columnLastLongitude)) {
                    double lastLatitude = cursor.getDouble(columnLastLatitude);
                    double lastLongitude = cursor.getDouble(columnLastLongitude);
                    lastLocation = new LatLng(lastLatitude, lastLongitude);
                }

                // 유저 데이터로 유저 객체를 만들어 리턴한다.
                user = new User(id, password, phone, isMale, isSignedIn, lastLocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return user;
    }

    // Get users who are currently signed in
    public List<User> getUsersSignedIn() {

        List<User> userList = new ArrayList<>();

        // 읽기용 DB 를 연다.
        SQLiteDatabase db = getReadableDatabase();

        // 모든 데이터를 가리키는 커서를 검색한다.
        String SELECT_ALL_USERS = "SELECT * FROM " + TABLE_USERS
                + " WHERE " + COLUMN_USER_IS_SIGNED_IN + " = " + 1;

        Cursor cursor = db.rawQuery(SELECT_ALL_USERS, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                    String password = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD));
                    String phone = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PHONE));
                    boolean isMale = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_MALE)) == 1);
                    boolean isSignedIn = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_SIGNED_IN)) == 1);

                    LatLng lastLocation = null;
                    int columnLastLatitude = cursor.getColumnIndex(COLUMN_USER_LAST_LATITUDE);
                    int columnLastLongitude = cursor.getColumnIndex(COLUMN_USER_LAST_LONGITUDE);
                    if (!cursor.isNull(columnLastLatitude) && !cursor.isNull(columnLastLongitude)) {
                        double lastLatitude = cursor.getDouble(columnLastLatitude);
                        double lastLongitude = cursor.getDouble(columnLastLongitude);
                        lastLocation = new LatLng(lastLatitude, lastLongitude);
                    }

                    // 유저 데이터로 유저 객체를 만들어 리스트에 삽입한다.
                    User user = new User(id, password, phone, isMale, isSignedIn, lastLocation);
                    userList.add(user);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return userList;
    }

    // Get all users registered in DB
    public List<User> getAllUsers() {

        List<User> userList = new ArrayList<>();

        // 읽기용 DB 를 연다.
        SQLiteDatabase db = getReadableDatabase();

        // 모든 데이터를 가리키는 커서를 검색한다.
        String SELECT_ALL_USERS = "SELECT * FROM " + TABLE_USERS;

        Cursor cursor = db.rawQuery(SELECT_ALL_USERS, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                    String password = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD));
                    String phone = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PHONE));
                    boolean isMale = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_MALE)) == 1);
                    boolean isSignedIn = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_SIGNED_IN)) == 1);

                    LatLng lastLocation = null;
                    int columnLastLatitude = cursor.getColumnIndex(COLUMN_USER_LAST_LATITUDE);
                    int columnLastLongitude = cursor.getColumnIndex(COLUMN_USER_LAST_LONGITUDE);
                    if (!cursor.isNull(columnLastLatitude) && !cursor.isNull(columnLastLongitude)) {
                        double lastLatitude = cursor.getDouble(columnLastLatitude);
                        double lastLongitude = cursor.getDouble(columnLastLongitude);
                        lastLocation = new LatLng(lastLatitude, lastLongitude);
                    }

                    // 유저 데이터로 유저 객체를 만들어 리스트에 삽입한다.
                    User user = new User(id, password, phone, isMale, isSignedIn, lastLocation);
                    userList.add(user);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return userList;
    }

    // Get all users registered in DB
    public List<User> SearchUsersSignedIn(String idPrefix) {

        List<User> userList = new ArrayList<>();

        // 읽기용 DB 를 연다.
        SQLiteDatabase db = getReadableDatabase();

        // 모든 데이터를 가리키는 커서를 검색한다.
        String SELECT_ALL_USERS = "SELECT * FROM " + TABLE_USERS
                + " WHERE " + COLUMN_USER_IS_SIGNED_IN + " = " + 1
                + " AND " + COLUMN_USER_ID + " LIKE '" + idPrefix + "%'";

        Cursor cursor = db.rawQuery(SELECT_ALL_USERS, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                    String password = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD));
                    String phone = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PHONE));
                    boolean isMale = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_MALE)) == 1);
                    boolean isSignedIn = (cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_SIGNED_IN)) == 1);

                    LatLng lastLocation = null;
                    int columnLastLatitude = cursor.getColumnIndex(COLUMN_USER_LAST_LATITUDE);
                    int columnLastLongitude = cursor.getColumnIndex(COLUMN_USER_LAST_LONGITUDE);
                    if (!cursor.isNull(columnLastLatitude) && !cursor.isNull(columnLastLongitude)) {
                        double lastLatitude = cursor.getDouble(columnLastLatitude);
                        double lastLongitude = cursor.getDouble(columnLastLongitude);
                        lastLocation = new LatLng(lastLatitude, lastLongitude);
                    }

                    // 유저 데이터로 유저 객체를 만들어 리스트에 삽입한다.
                    User user = new User(id, password, phone, isMale, isSignedIn, lastLocation);
                    userList.add(user);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return userList;
    }

}



