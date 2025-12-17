package app.controllers;

import app.dtos.RoleDTO;
import app.dtos.UserDTO;
import app.exceptions.ValidationException;
import app.services.AdminService;
import app.utils.SecurityUtils;
import io.javalin.http.Context;

import java.util.Map;

public class AdminController {


    private final AdminService service;

    public AdminController(AdminService service){
        this.service = service;
    }

    public void getAllUsers(Context ctx){
        ctx.json(service.getAllUsers());
    }

    public void deleteUser(Context ctx) {
        String usernameToDelete = ctx.pathParam("username");
        UserDTO requester = ctx.attribute("user");

        service.deleteUser(usernameToDelete, requester.getUsername());
        ctx.status(204);
    }

    public void getAllSongs(Context ctx) {
        ctx.json(service.getAllSongs());
    }

    public void updateUserRole(Context ctx) {
        String username = ctx.pathParam("username");
        String roleName = ctx.bodyAsClass(RoleDTO.class).getRoleName();

        service.grantRoleToUser(username, roleName);
        ctx.status(200).json(Map.of("message", "Role succesfully granted to user: " + username));
    }


    public void getUserDetails(Context ctx) {
        String username = ctx.pathParam("username");
        UserDTO dto = service.fetchUserDetails(username);
        ctx.json(dto);
    }
}
