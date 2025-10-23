import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.populators.Populator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

public class AdminTest {


    private static Javalin app;
    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUp() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        app = ApplicationConfig.startServer(7074, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7074;
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

    // Helper
    private String loginAndGetToken(String username, String password) {
        return given().contentType("application/json")
                .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .when().post("/login")
                .then().statusCode(200)
                .extract().path("token");
    }

    // Tests

    @Test
    void testGetAllUsers_asAdmin() {
        String token = loginAndGetToken("admin", "admin123");

        given().header("Authorization", "Bearer " + token)
                .when().get("/admin/users")
                .then().statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].username", notNullValue())
                .body("[0].roles", notNullValue());
    }

    @Test
    void testDeleteUser_asAdmin_success() {
        String token = loginAndGetToken("admin", "admin123");

        // Opret ny bruger
        given().contentType("application/json")
                .body("{\"username\":\"tempuser\",\"password\":\"pass\"}")
                .when().post("/register")
                .then().statusCode(201);

        // Slet bruger
        given().header("Authorization", "Bearer " + token)
                .pathParam("username", "tempuser")
                .when().delete("/admin/users/{username}")
                .then().statusCode(204);
    }

    @Test
    void testDeleteUser_selfDeletion_forbidden() {
        String token = loginAndGetToken("admin", "admin123");

        given().header("Authorization", "Bearer " + token)
                .pathParam("username", "admin")
                .when().delete("/admin/users/{username}")
                .then().statusCode(403)
                .body("message", containsString("Admins cannot delete themselves"));
    }

    @Test
    void testDeleteUser_notFound() {
        String token = loginAndGetToken("admin", "admin123");

        given().header("Authorization", "Bearer " + token)
                .pathParam("username", "ghost")
                .when().delete("/admin/users/{username}")
                .then().statusCode(404)
                .body("message", containsString("was not found"));
    }

    @Test
    void testGetAllSongs_asAdmin() {
        String token = loginAndGetToken("admin", "admin123");

        given().header("Authorization", "Bearer " + token)
                .when().get("/admin/songs")
                .then().statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].title", notNullValue());
    }

    @Test
    void testGrantRoleToUser_success() {
        String token = loginAndGetToken("admin", "admin123");

        // Opret ny bruger
        given().contentType("application/json")
                .body("{\"username\":\"roleuser\",\"password\":\"pass\"}")
                .when().post("/register")
                .then().statusCode(201);

        // Tildel rolle
        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .pathParam("username", "roleuser")
                .body("{\"roleName\":\"User\"}")
                .when().patch("/admin/users/{username}/role")
                .then().statusCode(200)
                .body("message", containsString("Role succesfully granted"));
    }

    @Test
    void testGrantRoleToUser_userNotFound() {
        String token = loginAndGetToken("admin", "admin123");

        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .pathParam("username", "ghost")
                .body("{\"roleName\":\"User\"}")
                .when().patch("/admin/users/{username}/role")
                .then().statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void testGrantRoleToUser_invalidRole() {
        String token = loginAndGetToken("admin", "admin123");

        // Opret ny bruger
        given().contentType("application/json")
                .body("{\"username\":\"invalidroleuser\",\"password\":\"pass\"}")
                .when().post("/register")
                .then().statusCode(201);

        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .pathParam("username", "invalidroleuser")
                .body("{\"roleName\":\"NonExistentRole\"}")
                .when().patch("/admin/users/{username}/role")
                .then().statusCode(404)
                .body("message", containsString("does not exist"));

    }

    @Test
    void testUserCannotAccessAdminRoutes() {
        String token = loginAndGetToken("user1", "test123");

        given().header("Authorization", "Bearer " + token)
                .when().get("/admin/users")
                .then().statusCode(403);
    }


}

