package app.routes;

import app.controllers.SecurityController;
import app.daos.SecurityDAO;
import app.services.SecurityService;
import app.utils.SecurityUtils;
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
        SecurityDAO dao = new SecurityDAO(emf);
        SecurityUtils securityUtils = new SecurityUtils();
        SecurityService securityService = new SecurityService(dao, securityUtils);
        this.mapper = new Utils().getObjectMapper();
        this.securityController = new SecurityController(securityService, mapper);
    }


    public EndpointGroup getRoutes() {
        return () -> {
            post("register", securityController.register());
            get("healthcheck", securityController::healthCheck);
        };
    }



}
