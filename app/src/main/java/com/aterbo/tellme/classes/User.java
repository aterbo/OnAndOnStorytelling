package com.aterbo.tellme.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ATerbo on 2/14/16.
 */
public class User implements Parcelable{
    private String userName;
    private String email;
    private String userID;


    public User(String name, String email, String userID){
        this.userName = name;
        this.email = email;
        this.userID = userID;
    }

    public User(){}

    public String getUserName() {
        return userName;
    }

    public String getUserID() {
        return userID;
    }

    public String getEmail() {
        return email;
    }

    protected User(Parcel in) {
        userName = in.readString();
        email = in.readString();
        userID = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(email);
        dest.writeString(userID);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
