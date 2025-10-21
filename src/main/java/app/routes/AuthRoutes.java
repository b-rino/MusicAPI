package app.routes;

import app.controllers.AuthController;
import app.daos.SecurityDAO;
import app.services.AuthService;
import app.utils.SecurityUtils;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthRoutes {

    private final ObjectMapper mapper;
    private final AuthController authController;

    public AuthRoutes(EntityManagerFactory emf){
        SecurityDAO dao = new SecurityDAO(emf);
        SecurityUtils securityUtils = new SecurityUtils();
        AuthService authService = new AuthService(dao, securityUtils);
        this.mapper = new Utils().getObjectMapper();
        this.authController = new AuthController(authService, mapper);
    }


    public EndpointGroup getRoutes() {
        return () -> {
            post("/register", authController.register());
            post("/login", authController.login());
        };
    }
}
