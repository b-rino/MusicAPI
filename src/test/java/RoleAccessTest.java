import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.populators.Populator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class RoleAccessTest {

    private static Javalin app;
    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUp() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        app = ApplicationConfig.startServer(7077, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7077;
        RestAssured.basePath = "/api/v1";
    }

    @BeforeEach
    void populateDatabase() {
        Populator.seedRoles(emf);
        Populator.seedUsers(emf);
        Populator.seedSongs(emf);
    }

    @AfterEach
    void clearDatabase() {
        Populator.clearDatabase(emf);
    }

    @AfterAll
    static void tearDown() {
        ApplicationConfig.stopServer(app);
    }

    private String loginAndGetToken(String username, String password) {
        return given().contentType("application/json")
                .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .when().post("/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    @Test
    void testUserAccessingAdminEndpoint_shouldFail() {
        String token = loginAndGetToken("user1", "test123");

        given().header("Authorization", "Bearer " + token)
                .when().get("/admin/users")
                .then().statusCode(403)
                .body("error", containsString("Access denied"));
    }

    @Test
    void testAdminAccessingUserEndpoint_shouldFail() {
        String token = loginAndGetToken("admin", "admin123");

        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"Admin Playlist\"}")
                .when().post("/playlists")
                .then().statusCode(403)
                .body("message", containsString("User was not authorized"));
    }

    @Test
    void testUnauthenticatedAccess_shouldFail() {
        given().when().get("/admin/users")
                .then().statusCode(401)
                .body("message", containsString("Authorization header is missing"));

        given().when().get("/playlists")
                .then().statusCode(401)
                .body("message", containsString("Authorization header is missing"));
    }
}
