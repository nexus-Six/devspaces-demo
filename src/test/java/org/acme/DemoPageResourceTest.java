package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class DemoPageResourceTest {
    @Test
    void testIndexShowsVersion() {
        given()
          .when().get("/")
          .then()
             .statusCode(200)
             .body(containsString("App version 2.0.0-SNAPSHOT"))
             .body(containsString("Quarkus Mission Control"));
    }
}
