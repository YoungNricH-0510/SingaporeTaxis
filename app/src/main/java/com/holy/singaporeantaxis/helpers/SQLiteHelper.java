package com.holy.singaporeantaxis.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.holy.singaporeantaxis.models.User;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SQLiteHelper extends SQLiteOpenHelper {

    // 데이터베이스 정보
    public static final String DATABASE_NAME = "db";
    public static final int DATABASE_VERSION = 1;

    // 유저 테이블 정보
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_IS_MALE = "isMale";


    // 데이터베이스 헬퍼 객체
    private static SQLiteHelper instance;

    // 데이터베이스 헬퍼 객체 구하기.
    public static synchronized SQLiteHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }

    // 생성자
    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 유저 테이블 생성
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS
                + "("
                + COLUMN_USER_ID + " TEXT PRIMARY KEY, "
                + COLUMN_USER_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_USER_PHONE + " TEXT NOT NULL, "
                + COLUMN_USER_IS_MALE + " INTEGER NOT NULL"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // 버전 교체
        if (newVersion != oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }


    // 유저 등록
    public void addUser(User user) {

        // 쓰기용 DB 를 연다.
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

            // 데이터베이스에 values 를 입력한다.
            db.insertOrThrow(TABLE_USERS, null, values);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    // 아이디로 유저 검색
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

                // 유저 데이터로 유저 객체를 만들어 리턴한다.
                user = new User(id, password, phone, isMale);
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

                // 유저 데이터로 유저 객체를 만들어 리턴한다.
                user = new User(id, password, phone, isMale);
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

    // 모든 아이디 가져오기
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

                    // 유저 데이터로 유저 객체를 만들어 리스트에 삽입한다.
                    User user = new User(id, password, phone, isMale);
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


