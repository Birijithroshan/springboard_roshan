package org.automation.api;

import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import org.testng.annotations.BeforeClass;

import static io.restassured.config.DecoderConfig.decoderConfig;

public class ApiBaseTest {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";

        // Avoid NullPointer for default charset
        RestAssured.config = RestAssured.config()
                .decoderConfig(decoderConfig().defaultContentCharset("UTF-8"));
    }
}
