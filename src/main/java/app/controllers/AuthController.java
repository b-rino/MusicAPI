package app.controllers;

import app.dtos.UserDTO;
import app.entities.Role;
import app.entities.User;
import app.exceptions.ValidationException;
import app.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.Set;
import java.util.stream.Collectors;

public class AuthController {


    private final AuthService authService;
    private final ObjectMapper mapper;


    public AuthController(AuthService authService, ObjectMapper mapper){
        this.authService = authService;
        this.mapper = mapper;
    }


    public Handler register() {
        return ctx -> {
            User incomingUser = ctx.bodyAsClass(User.class);
            if(incomingUser != null){
                UserDTO userDTO = authService.register(incomingUser.getUsername(), incomingUser.getPassword());

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
            User checkedUser = authService.getVerifiedUser(incomingUser.getUsername(), incomingUser.getPassword());

            if(checkedUser == null){
                throw new ValidationException("Invalid username or password");
            }

            Set<String> roleNames = checkedUser.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());
            UserDTO userDTO = new UserDTO(checkedUser.getUsername(), roleNames);

            String token = authService.createToken(userDTO);

            ObjectNode on = mapper.createObjectNode().
                    put("token", token).
                    put("username", userDTO.getUsername());
            ctx.json(on).status(200);

        };
    }




}
