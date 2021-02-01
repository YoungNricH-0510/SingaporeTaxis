package com.holy.singaporeantaxis.models;

public class User {

    private final String id;
    private final String password;
    private final String phone;
    private final boolean isMale;

    public User(String id, String password, String phone, boolean isMale) {
        this.id = id;
        this.password = password;
        this.phone = phone;
        this.isMale = isMale;
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
}
