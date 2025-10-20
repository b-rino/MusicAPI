package app.controllers;

import app.dtos.UserDTO;
import app.entities.User;
import app.exceptions.ValidationException;
import app.services.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class SecurityController {


    private final SecurityService securityService;
    private final ObjectMapper mapper;


    public SecurityController(SecurityService securityService, ObjectMapper mapper){
        this.securityService = securityService;
        this.mapper = mapper;
    }


    public void healthCheck(Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }

    public Handler register() {
        return ctx -> {
            User incomingUser = ctx.bodyAsClass(User.class);
            if(incomingUser != null){
                UserDTO userDTO = securityService.register(incomingUser.getUsername(), incomingUser.getPassword());
                //TODO: lad evt være med at returnere token her og i stedet en msg om at de skal logge ind!
                String token = securityService.createToken(userDTO);

                ObjectNode on = mapper.createObjectNode()
                        .put("token", token)
                        .put("username", userDTO.getUsername());

                ctx.json(on).status(201);
            } else {
                throw new ValidationException("Could not create user!");
            }
        };
    }
}
