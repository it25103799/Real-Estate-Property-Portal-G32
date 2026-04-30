package com.realestate.portal.service;

import com.realestate.portal.model.User;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginService {
    private final String filePath;

    public LoginService(String filePath) {
        this.filePath = filePath;
    }

    public User authenticate(String email, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 5 && data[1].equals(email) && data[3].equals(password)) {
                    // username,email,phoneNumber,password,role
                    return new User(data[0], data[1], data[2], data[3], data[4]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
        return null;
    }
}