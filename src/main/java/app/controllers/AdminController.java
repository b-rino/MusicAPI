package app.controllers;

import app.services.AdminService;
import app.utils.SecurityUtils;
import io.javalin.http.Context;

public class AdminController {


    private final AdminService service;

    public AdminController(AdminService service){
        this.service = service;
    }



    public void getAllUsers(Context ctx){
        ctx.json(service.getAllUsers());
    }
}
