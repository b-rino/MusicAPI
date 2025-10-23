import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.populators.Populator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PlaylistTest {

    private static Javalin app;
    private static EntityManagerFactory emf;

    @BeforeAll
    static void setUp() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();
        app = ApplicationConfig.startServer(7076, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7076;
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
    void testCreatePlaylist_success() {
        String token = loginAndGetToken("user1", "test123");

        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"My Playlist\"}")
                .when().post("/playlists")
                .then().statusCode(201)
                .body("name", equalTo("My Playlist"))
                .body("id", notNullValue());
    }

    @Test
    void testGetAllPlaylistsForUser_success() {
        String token = loginAndGetToken("user1", "test123");


        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"Workout Mix\"}")
                .when().post("/playlists")
                .then().statusCode(201);


        given().header("Authorization", "Bearer " + token)
                .when().get("/playlists")
                .then().statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("[0].name", notNullValue());
    }

    @Test
    void testAddSongToPlaylist_success() {
        String token = loginAndGetToken("user1", "test123");

        int playlistId = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"Chill\"}")
                .when().post("/playlists")
                .then().extract().path("id");

        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .pathParam("id", playlistId)
                .body("{\"externalId\":999,\"title\":\"Test Song\",\"artist\":\"Test Artist\",\"album\":\"Test Album\"}")
                .when().post("/playlists/{id}/songs")
                .then().statusCode(200)
                .body("songs", hasSize(1))
                .body("songs[0].title", equalTo("Test Song"));
    }

    @Test
    void testGetSongsInPlaylist_success() {
        String token = loginAndGetToken("user1", "test123");

        int playlistId = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"Focus\"}")
                .when().post("/playlists")
                .then().extract().path("id");

        // Tilføj sang
        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .pathParam("id", playlistId)
                .body("{\"externalId\":123,\"title\":\"Focus Track\",\"artist\":\"Artist\",\"album\":\"Album\"}")
                .when().post("/playlists/{id}/songs")
                .then().statusCode(200);

        // Hent sange
        given().header("Authorization", "Bearer " + token)
                .pathParam("id", playlistId)
                .when().get("/playlists/{id}/songs")
                .then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].title", equalTo("Focus Track"));
    }

    @Test
    void testUpdatePlaylistName_success() {
        String token = loginAndGetToken("user1", "test123");

        int playlistId = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"Old Name\"}")
                .when().post("/playlists")
                .then().extract().path("id");

        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .pathParam("id", playlistId)
                .body("{\"name\":\"New Name\"}")
                .when().put("/playlists/{id}")
                .then().statusCode(200)
                .body("name", equalTo("New Name"));
    }

    @Test
    void testDeletePlaylist_success() {
        String token = loginAndGetToken("user1", "test123");

        int playlistId = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"Delete Me\"}")
                .when().post("/playlists")
                .then().extract().path("id");

        given().header("Authorization", "Bearer " + token)
                .pathParam("id", playlistId)
                .when().delete("/playlists/{id}")
                .then().statusCode(204);
    }

    @Test
    void testRemoveSongFromPlaylist_success() {
        String token = loginAndGetToken("user1", "test123");

        int playlistId = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"Edit Me\"}")
                .when().post("/playlists")
                .then().extract().path("id");

        // Tilføj sang
        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .pathParam("id", playlistId)
                .body("{\"externalId\":321,\"title\":\"Remove Me\",\"artist\":\"Artist\",\"album\":\"Album\"}")
                .when().post("/playlists/{id}/songs")
                .then().statusCode(200);

        // Hent sangens ID
        int songId = given().header("Authorization", "Bearer " + token)
                .pathParam("id", playlistId)
                .when().get("/playlists/{id}/songs")
                .then().extract().path("[0].id");

        // Fjern sang
        given().header("Authorization", "Bearer " + token)
                .pathParam("playlistId", playlistId)
                .pathParam("songId", songId)
                .when().delete("/playlists/{playlistId}/songs/{songId}")
                .then().statusCode(204);
    }


    @Test
    void testCreatePlaylist_missingName_fail() {
        String token = loginAndGetToken("user1", "test123");

        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"\"}")
                .when().post("/playlists")
                .then().statusCode(400)
                .body("message", containsString("Playlist name is required"));
    }

    @Test
    void testCreatePlaylist_duplicateName() {
        String token = loginAndGetToken("user1", "test123");

        // Første oprettelse
        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"My Mix\"}")
                .when().post("/playlists")
                .then().statusCode(201);

        // Dublet
        given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"My Mix\"}")
                .when().post("/playlists")
                .then().statusCode(409)
                .body("message", containsString("already have a playlist"));
    }

    @Test
    void testAddSongToOthersPlaylist_fail() {
        // Oprettelse af to forskellige brugere med User-role
        String ownerToken = loginAndGetToken("user2", "test321"); // ejer af playlist
        String otherUserToken = loginAndGetToken("user1", "test123"); // forsøger at tilføje sang

        // Opret playlist som user2
        int playlistId = given().header("Authorization", "Bearer " + ownerToken)
                .contentType("application/json")
                .body("{\"name\":\"Private Mix\"}")
                .when().post("/playlists")
                .then().statusCode(201)
                .extract().path("id");

        // user1 forsøger at tilføje sang til user2's playlist
        given().header("Authorization", "Bearer " + otherUserToken)
                .contentType("application/json")
                .pathParam("id", playlistId)
                .body("{\"externalId\":1,\"title\":\"Hack\",\"artist\":\"Sneaky\",\"album\":\"Oops\"}")
                .when().post("/playlists/{id}/songs")
                .then().statusCode(403)
                .body("message", containsString("do not own this playlist"));
    }



    @Test
    void testRemoveNonexistentSong_fail() {
        String token = loginAndGetToken("user1", "test123");

        int playlistId = given().header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"Ghost Tracks\"}")
                .when().post("/playlists")
                .then().extract().path("id");

        given().header("Authorization", "Bearer " + token)
                .pathParam("playlistId", playlistId)
                .pathParam("songId", 9999)
                .when().delete("/playlists/{playlistId}/songs/{songId}")
                .then().statusCode(404)
                .body("message", containsString("Song not found"));
    }


    @Test
    void testDeleteOthersPlaylist_fail() {
        String adminToken = loginAndGetToken("user2", "test321");
        String userToken = loginAndGetToken("user1", "test123");

        int playlistId = given().header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("{\"name\":\"Admin Only\"}")
                .when().post("/playlists")
                .then().extract().path("id");

        given().header("Authorization", "Bearer " + userToken)
                .pathParam("id", playlistId)
                .when().delete("/playlists/{id}")
                .then().statusCode(403)
                .body("message", containsString("You do not own this playlist"));
    }


    @Test
    void testUpdateOthersPlaylistName_fail() {
        String adminToken = loginAndGetToken("user2", "test321");
        String userToken = loginAndGetToken("user1", "test123");

        int playlistId = given().header("Authorization", "Bearer " + adminToken)
                .contentType("application/json")
                .body("{\"name\":\"Secret Admin\"}")
                .when().post("/playlists")
                .then().extract().path("id");

        given().header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .pathParam("id", playlistId)
                .body("{\"name\":\"Hacked\"}")
                .when().put("/playlists/{id}")
                .then().statusCode(403)
                .body("message", containsString("You do not own this playlist"));
    }






}