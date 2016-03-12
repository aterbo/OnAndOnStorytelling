package com.aterbo.tellme.classes;

/**
 * Created by ATerbo on 2/14/16.
 */
public class User {
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
}
