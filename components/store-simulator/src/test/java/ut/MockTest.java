package ut;

import static org.mockito.Mockito.when;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

@QuarkusTest
public class MockTest {

    @InjectMock
    GreetingService greetingService;

    @Test
    public void testGreeting() {
        when(greetingService.greet()).thenReturn("hi");
        given()
                .when().get("/greeting")
                .then()
                .statusCode(200)
                .body(is("hi")); 
    }

    @Path("greeting")
    public static class GreetingResource {

        final GreetingService greetingService;

        public GreetingResource(GreetingService greetingService) {
            this.greetingService = greetingService;
        }

        @GET
        @Produces("text/plain")
        public String greet() {
            return greetingService.greet();
        }
    }

    @ApplicationScoped
    public static class GreetingService {
        public String greet(){
            return "hello";
        }
    }
}