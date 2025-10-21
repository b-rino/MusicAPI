package app.routes;

import app.controllers.PlaylistController;
import app.daos.PlaylistDAO;
import app.daos.UserDAO;
import app.enums.Role;
import app.services.PlaylistService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class PlaylistRoutes {

    private final PlaylistController controller;

    public PlaylistRoutes(EntityManagerFactory emf){
        PlaylistDAO playlistDAO = new PlaylistDAO(emf);
        UserDAO userDAO = new UserDAO(emf);
        PlaylistService service = new PlaylistService(playlistDAO);
        this.controller = new PlaylistController(service, userDAO);
    }

    public EndpointGroup getRoutes() {
        return () -> {
            post("/playlists", controller::create, Role.USER);
            get("/playlists", controller::getAllPlaylistsForUser, Role.USER);
            post("playlists/{id}/songs", controller::addSong, Role.USER);
            get("playlists/{id}/songs", controller::getSongs, Role.USER);
        };
    }
}
