package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightBookingTestData {
    private String fromCity;
    private String toCity;
    private String passengerName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String cardType;
    private String cardNumber;
    private String cardMonth;
    private String cardYear;
    private String nameOnCard;

    // Factory methods for different test scenarios
    public static FlightBookingTestData validBookingData() {
        return new FlightBookingTestData(
            "Boston",
            "London",
            "John Doe",
            "123 Test Street",
            "Test City",
            "Test State",
            "12345",
            "Visa",
            "4111111111111111",
            "12",
            "2025",
            "John Doe"
        );
    }

    public static FlightBookingTestData internationalBookingData() {
        return new FlightBookingTestData(
            "Portland",
            "Rome",
            "Jane Smith",
            "456 International Ave",
            "Global City",
            "World State",
            "54321",
            "American Express",
            "378282246310005",
            "06",
            "2026",
            "Jane Smith"
        );
    }

    public static FlightBookingTestData domesticBookingData() {
        return new FlightBookingTestData(
            "San Diego",
            "New York",
            "Mike Johnson",
            "789 Domestic Blvd",
            "Local City",
            "Home State",
            "67890",
            "Mastercard",
            "5555555555554444",
            "03",
            "2027",
            "Mike Johnson"
        );
    }
}
