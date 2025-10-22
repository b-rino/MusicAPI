package app.routes;

import app.controllers.AdminController;
import app.daos.UserDAO;
import app.enums.Role;
import app.services.AdminService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AdminRoutes {

    private final AdminController controller;

    public AdminRoutes(EntityManagerFactory emf){
        UserDAO dao = new UserDAO(emf);
        AdminService service = new AdminService(dao);
        this.controller = new AdminController(service);
    }


    public EndpointGroup getRoutes() {
        return () -> {
            get("/users", controller::getAllUsers, Role.ADMIN);
        };
    }
}
