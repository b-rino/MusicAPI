package app.routes;

import app.controllers.SystemController;
import app.enums.Role;
import app.services.DeezerClient;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class SystemRoutes {

    private final SystemController controller;

    public SystemRoutes(DeezerClient dc){
        this.controller = new SystemController(dc);
    }


    public EndpointGroup getRoutes() {
        return () -> {
            get("healthcheck", controller::healthCheck, Role.ANYONE);
            get("songs/search", controller::searchExternal, Role.ANYONE);
        };
    }
}
