import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.populators.Populator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthTest {

    private static Javalin app;
    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUp() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        app = ApplicationConfig.startServer(7078, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7078;
        RestAssured.basePath = "/api/v1";
    }

    @BeforeEach
    void populateDatabase() {
        Populator.seedRoles(emf);
        Populator.seedUsers(emf); // includes user1 and admin
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
    void testRegister_success() {
        given().contentType("application/json")
                .body("{\"username\":\"newuser\",\"password\":\"newpass\"}")
                .when().post("/register")
                .then().statusCode(201)
                .body("msg", containsString("successfully created"))
                .body("username", equalTo("newuser"));
    }


    @Test
    void testRegister_existingUsername_fail() {
        given().contentType("application/json")
                .body("{\"username\":\"user1\",\"password\":\"test123\"}")
                .when().post("/register")
                .then().statusCode(409)
                .body("message", containsString("Username not available"));
    }


    @Test
    void testRegister_emptyBody_fail() {
        given().contentType("application/json")
                .body("{}")
                .when().post("/register")
                .then().statusCode(400)
                .body("message", containsString("Username and password are required"));
    }


    @Test
    void testLogin_success() {
        given().contentType("application/json")
                .body("{\"username\":\"user1\",\"password\":\"test123\"}")
                .when().post("/login")
                .then().statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("user1"));
    }


    @Test
    void testLogin_wrongPassword_fail() {
        given().contentType("application/json")
                .body("{\"username\":\"user1\",\"password\":\"wrongpass\"}")
                .when().post("/login")
                .then().statusCode(403)
                .body("message", containsString("Invalid username or password"));
    }


    @Test
    void testLogin_unknownUser_fail() {
        given().contentType("application/json")
                .body("{\"username\":\"ghost\",\"password\":\"nopass\"}")
                .when().post("/login")
                .then().statusCode(403)
                .body("message", containsString("Invalid username or password"));
    }


    //Uden nogle check, da vi kun bruger denne metode i dette kontrollerde testmiljø!
    private String loginAndGetToken(String username, String password) {
        return given().contentType("application/json")
                .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .when().post("/login")
                .then().statusCode(200)
                .extract().path("token");
    }


    @Test
    void testValidToken_accessProtectedEndpoint() {
        String token = loginAndGetToken("user1", "test123");

        given().header("Authorization", "Bearer " + token)
                .when().get("/playlists")
                .then().statusCode(200);
    }

    @Test
    void testMissingToken_fail() {
        given().when().get("/playlists")
                .then().statusCode(401)
                .body("message", containsString("Authorization header is missing"));
    }

    @Test
    void testMalformedTokenHeader_fail() {
        given().header("Authorization", "Token abc.def.ghi")
                .when().get("/playlists")
                .then().statusCode(401)
                .body("message", containsString("Authorization header is malformed"));
    }

    @Test
    void testInvalidTokenSignature_fail() {
        String fakeToken = "Bearer abc.def.ghi"; 

        given().header("Authorization", fakeToken)
                .when().get("/playlists")
                .then().statusCode(401)
                .body("error", containsString("Token verification failed"))
                .body("message", containsString("Token is malformed"));
    }

    @Test
    void testTokenValidButUserDeleted_fail() {
        String token = loginAndGetToken("user1", "test123");

        // Slet user1 som admin
        String adminToken = loginAndGetToken("admin", "admin123");
        given().header("Authorization", "Bearer " + adminToken)
                .pathParam("username", "user1")
                .when().delete("/admin/users/{username}")
                .then().statusCode(204);

        // Brug den gamle token
        given().header("Authorization", "Bearer " + token)
                .when().get("/playlists")
                .then().statusCode(401)
                .body("message", containsString("Token is valid but user could not be resolved"));
    }
}
