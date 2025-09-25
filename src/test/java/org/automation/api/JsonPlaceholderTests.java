package org.automation.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.automation.listeners.TestSuiteListener;
import org.testng.Reporter;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Listeners(TestSuiteListener.class)
public class JsonPlaceholderTests extends ApiBaseTest {

    @Test(description = "Get all posts")
    public void testGetAllPosts() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US201");

        Response response = given().contentType(ContentType.JSON)
                .when().get("/posts")
                .then().statusCode(200).body("size()", greaterThan(0))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", "GET /posts");
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Get single post by ID")
    public void testGetSinglePost() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US202");

        Response response = given().contentType(ContentType.JSON)
                .when().get("/posts/1")
                .then().statusCode(200).body("id", equalTo(1))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", "GET /posts/1");
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Create a new post")
    public void testCreatePost() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US203");

        String payload = "{ \"title\":\"foo\", \"body\":\"bar\", \"userId\":1 }";

        Response response = given().contentType(ContentType.JSON).body(payload)
                .when().post("/posts")
                .then().statusCode(201).body("title", equalTo("foo"))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", payload);
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Update an existing post")
    public void testUpdatePost() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US204");

        String payload = "{ \"id\":1, \"title\":\"updated\", \"body\":\"bar\", \"userId\":1 }";

        Response response = given().contentType(ContentType.JSON).body(payload)
                .when().put("/posts/1")
                .then().statusCode(200).body("title", equalTo("updated"))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", payload);
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Patch an existing post")
    public void testPatchPost() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US205");

        String payload = "{ \"title\":\"patched\" }";

        Response response = given().contentType(ContentType.JSON).body(payload)
                .when().patch("/posts/1")
                .then().statusCode(200).body("title", equalTo("patched"))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", payload);
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Delete a post")
    public void testDeletePost() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US206");

        Response response = when().delete("/posts/1")
                .then().statusCode(200)
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", "DELETE /posts/1");
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Get all users")
    public void testGetAllUsers() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US207");

        Response response = when().get("/users")
                .then().statusCode(200).body("size()", greaterThan(0))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", "GET /users");
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Get single user by ID")
    public void testGetSingleUser() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US208");

        Response response = when().get("/users/1")
                .then().statusCode(200).body("id", equalTo(1))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", "GET /users/1");
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Get comments for a post")
    public void testGetCommentsForPost() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US209");

        Response response = when().get("/posts/1/comments")
                .then().statusCode(200).body("size()", greaterThan(0))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", "GET /posts/1/comments");
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }

    @Test(description = "Create a comment")
    public void testCreateComment() {
        Reporter.getCurrentTestResult().setAttribute("US_ID", "US210");

        String payload = "{ \"postId\":1, \"name\":\"Test\", \"email\":\"test@test.com\", \"body\":\"Test comment\" }";

        Response response = given().contentType(ContentType.JSON).body(payload)
                .when().post("/comments")
                .then().statusCode(201).body("name", equalTo("Test"))
                .extract().response();

        Reporter.getCurrentTestResult().setAttribute("requestPayload", payload);
        Reporter.getCurrentTestResult().setAttribute("responseBody", response.asString());
    }
}
