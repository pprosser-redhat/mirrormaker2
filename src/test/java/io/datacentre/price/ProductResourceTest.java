package io.datacentre.price;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ProductResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/stores")
          .then()
             .statusCode(200)
             .body(is("Hello from RESTEasy Reactive"));
    }
}