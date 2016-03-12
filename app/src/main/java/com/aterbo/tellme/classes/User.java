package com.aterbo.tellme.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ATerbo on 2/14/16.
 */
public class User implements Parcelable{
    private String name;
    private String userName;

    public User(String name, String userName){
        this.name = name;
        this.userName = userName;
    }

    public User(String name){
        this.name = name;
        this.userName = "";
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    protected User(Parcel in) {
        name = in.readString();
        userName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(userName);
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
