package app.routes;

import app.controllers.ExternalSongController;
import app.enums.Role;
import app.services.ExternalSongService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class ExternalSongRoutes {

    private final ExternalSongController controller;

    public ExternalSongRoutes(ExternalSongService ess){
        this.controller = new ExternalSongController(ess);
    }


    public EndpointGroup getRoutes() {
        return () -> {
            get("songs/search", controller::searchExternal, Role.ANYONE);
            get("songs/track/{trackId}", controller::searchByTrackId, Role.ANYONE);
        };
    }
}
