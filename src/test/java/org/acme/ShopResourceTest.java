package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class ShopResourceTest {

    @Test
    void shopPageIsServed() {
        given()
          .when().get("/shop/")
          .then()
             .statusCode(200)
             .body(containsString("Quarkus Merch"))
             .body(containsString("Quarkus rocks"))
             .body(containsString("Add to cart"))
             .body(containsString("Checkout"));
    }

    @Test
    void shopScriptExposesDemoFixFlag() {
        given()
          .when().get("/shop/app.js")
          .then()
             .statusCode(200)
             .body(containsString("CHECKOUT_BUG"))
             .body(containsString("SUMMIT-1847"));
    }
}
