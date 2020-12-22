package com.javanes.micro.quarkus.camel.gateway;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class AppGatewayTest {
    
    @Test
    public void appTest(){
        given()
            .when()
                .get("/v1/hello-controller/hello")
            .then()
                .statusCode(200)
                .body(is("{\"response\":\"Hello world !!\"}"));

        given()
            .when()
                .get("/v1/hello-controller/hello/Alejandro")
            .then()
                .statusCode(200)
                .body(is("{\"response\":\"Hello Alejandro !!\"}"));
        
        given()
            .when()
                .header(new Header("Content-Type", "application/json"))
                .body("{\"name\":\"Alejandro\"}")
                .post("/v1/hello-controller/hello")
            .then()
                .statusCode(200)
                .body(is("{\"response\":\"Hello Alejandro !!\"}"));

    }
}
