package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class JiraTicketResourceTest {

    @Test
    void ticketPageShowsDevSpacesLink() {
        given()
          .when().get("/jira")
          .then()
             .statusCode(200)
             .body(containsString("SUMMIT-1847"))
             .body(containsString("Open in OpenShift Dev Spaces"))
             .body(containsString("/tree/v1"))
             .body(containsString("OpenJDK 17"))
             .body(containsString("devfile.yaml"));
    }
}
