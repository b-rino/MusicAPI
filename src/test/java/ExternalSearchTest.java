import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.populators.Populator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ExternalSearchTest {

    private static Javalin app;
    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUp() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        app = ApplicationConfig.startServer(7079, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7079;
        RestAssured.basePath = "/api/v1";
    }

    @BeforeEach
    void populateDatabase() {
        Populator.seedRoles(emf);
        Populator.seedUsers(emf);
    }

    @AfterEach
    void clearDatabase() {
        Populator.clearDatabase(emf);
    }

    @AfterAll
    static void tearDown() {
        ApplicationConfig.stopServer(app);
    }


    @Test
    void testSearchExternal_success() {
        given().queryParam("query", "Imagine Dragons")
                .when().get("/songs/search")
                .then().statusCode(200)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].title", notNullValue())
                .body("[0].artist", notNullValue())
                .body("[0].album", notNullValue());
    }


    @Test
    void testSearchExternal_missingQuery_shouldFail() {
        given().when().get("/songs/search")
                .then().statusCode(400)
                .body("message", containsString("Missing query parameter"));
    }
}
