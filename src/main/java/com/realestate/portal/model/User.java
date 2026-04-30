package com.realestate.portal.model;

public class User {

    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private String role;

    public User() {}

    public User(String username, String email, String phoneNumber, String password, String role) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
    }

    // Overloaded constructor for cases where phone number might be missing
    public User(String username, String email, String password, String role) {
        this(username, email, "", password, role);
    }


    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
