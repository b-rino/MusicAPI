package app.controllers;

import app.dtos.UserDTO;
import app.entities.Role;
import app.entities.User;
import app.exceptions.ValidationException;
import app.services.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
//                String token = securityService.createToken(userDTO);

                ObjectNode on = mapper.createObjectNode()
                        .put("msg", "User successfully created. Please log in")
                        .put("username", userDTO.getUsername());

                ctx.json(on).status(201);
            } else {
                throw new ValidationException("Could not create user");
            }
        };
    }

    public Handler login(){
        return ctx -> {
            User incomingUser;
            try{
                incomingUser = ctx.bodyAsClass(User.class);
            } catch (Exception e){
                throw new ValidationException("Invalid request body");
            }
            User checkedUser = securityService.getVerifiedUser(incomingUser.getUsername(), incomingUser.getPassword());

            if(checkedUser == null){
                throw new ValidationException("Invalid username or password");
            }

            Set<String> roleNames = checkedUser.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
            UserDTO userDTO = new UserDTO(checkedUser.getUsername(), roleNames);

            String token = securityService.createToken(userDTO);

            ObjectNode on = mapper.createObjectNode().
                    put("token", token).
                    put("username", userDTO.getUsername());
            ctx.json(on).status(200);

        };
    }


}
