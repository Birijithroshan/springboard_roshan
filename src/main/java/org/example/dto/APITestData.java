package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APITestData {
    private String name;
    private String job;
    private String email;
    private String password;
    private int id;

    // Factory methods for different API test scenarios
    public static APITestData createUserData() {
        return new APITestData(
            "John Doe",
            "QA Engineer",
            null,
            null,
            0
        );
    }

    public static APITestData updateUserData() {
        return new APITestData(
            "Jane Smith",
            "Senior QA Engineer",
            null,
            null,
            2
        );
    }

    public static APITestData registerUserData() {
        return new APITestData(
            null,
            null,
            "eve.holt@reqres.in",
            "pistol",
            0
        );
    }

    public static APITestData loginUserData() {
        return new APITestData(
            null,
            null,
            "eve.holt@reqres.in",
            "cityslicka",
            0
        );
    }

    public static APITestData invalidRegisterData() {
        return new APITestData(
            null,
            null,
            "sydney@fife",
            null, // Missing password
            0
        );
    }

    public static APITestData invalidLoginData() {
        return new APITestData(
            null,
            null,
            "peter@klaven",
            null, // Missing password
            0
        );
    }
}
