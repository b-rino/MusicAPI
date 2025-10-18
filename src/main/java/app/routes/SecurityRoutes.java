package app.routes;

import app.controllers.SecurityController;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class SecurityRoutes {

    private final ObjectMapper mapper;
    private final SecurityController securityController;

    public SecurityRoutes(EntityManagerFactory emf){
        this.mapper = new Utils().getObjectMapper();
        this.securityController = new SecurityController();
    }


    public EndpointGroup getRoutes() {
        return () -> {
            get("healthcheck", securityController::healthCheck);
        };
    }



}
