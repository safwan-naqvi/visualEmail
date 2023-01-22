package com.example.visualemail.Model;

public class Account {
    private static String email;
    private static String password;

    public Account(){

    }
    public Account(String email,String password){
        this.email = email;
        this.password = password;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        Account.email = email;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Account.password = password;
    }
}
