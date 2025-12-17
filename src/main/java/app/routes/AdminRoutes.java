package app.routes;

import app.controllers.AdminController;
import app.daos.SongDAO;
import app.daos.UserDAO;
import app.enums.Role;
import app.services.AdminService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class AdminRoutes {

    private final AdminController controller;

    public AdminRoutes(EntityManagerFactory emf){
        UserDAO userDAO = new UserDAO(emf);
        SongDAO songDAO = new SongDAO(emf);
        AdminService service = new AdminService(userDAO, songDAO);
        this.controller = new AdminController(service);
    }


    public EndpointGroup getRoutes() {
        return () -> {
            get("users", controller::getAllUsers, Role.ADMIN);
            get("users/{username}", controller::getUserDetails, Role.ADMIN);
            delete("users/{username}", controller::deleteUser, Role.ADMIN);
            get("songs", controller::getAllSongs, Role.ADMIN);
            patch("users/{username}/role", controller::updateUserRole, Role.ADMIN);
        };
    }
}
