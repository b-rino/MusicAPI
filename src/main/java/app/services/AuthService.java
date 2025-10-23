package app.services;

import app.daos.AuthDAO;
import app.dtos.UserDTO;
import app.entities.Role;
import app.entities.User;
import app.exceptions.*;
import app.utils.SecurityUtils;
import app.utils.Utils;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthService {

    private final AuthDAO dao;
    private final SecurityUtils securityUtils;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public AuthService(AuthDAO dao, SecurityUtils securityUtils){
        this.dao = dao;
        this.securityUtils = securityUtils;
    }

    public String createToken(UserDTO user) throws TokenCreationException {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            return securityUtils.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (TokenCreationException e) {
            logger.error("Token creation failed for user '{}': {}", user.getUsername(), e.getMessage(), e);
            throw new TokenCreationException("Could not create token", e);
        }
    }


    public UserDTO register(String username, String password) throws EntityAlreadyExistsException, EntityNotFoundException {
        if (dao.existingUsername(username)) {
            throw new EntityAlreadyExistsException("Username not available");
        }

        User newUser = dao.createUser(username, password);
        User newUserWithRoles;
        try {
            newUserWithRoles = dao.addUserRole(newUser.getUsername(), "User");
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Either user or role doesn't exist!");
        }

        Set<String> roleNames = newUserWithRoles.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        return new UserDTO(newUserWithRoles.getUsername(), roleNames);
    }

    public User getVerifiedUser(String username, String password) throws ValidationException {
        try{
            return dao.getVerifiedUser(username, password);
        } catch (Exception e){
            throw new ValidationException("Invalid username or password");
        }
    }


    public UserDTO verifyToken(String token) throws TokenVerificationException {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED
                ? System.getenv("SECRET_KEY")
                : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            if (!securityUtils.tokenIsValid(token, SECRET)) {
                logger.warn("Token signature invalid for token");
                throw new TokenVerificationException("Token signature is invalid");
            }

            if (!securityUtils.tokenNotExpired(token)) {
                logger.warn("Token expired for token");
                throw new TokenVerificationException("Token has expired");
            }

            return securityUtils.getUserWithRolesFromToken(token);

        } catch (ParseException e) {
            logger.warn("Token parsing failed: {}", e.getMessage());
            throw new TokenVerificationException("Token is malformed", e);
            //Smider dem som jeg har lavet i hvert if-statement i stedet for at override dem med en ny
        } catch (TokenVerificationException e) {
            throw e;
        }
    }

    public String getToken(Context ctx) throws TokenVerificationException {
        String header = ctx.header("Authorization");
        if (header == null || header.isBlank()) {
            logger.warn("Missing Authorization header at [{}] {}", ctx.method().toString(), ctx.path());
            throw new TokenVerificationException("Authorization header is missing");
        }

        String[] parts = header.split(" ");
        if (parts.length != 2 || !parts[0].equalsIgnoreCase("Bearer") || parts[1].isBlank()) {
            logger.warn("Malformed Authorization header at [{}] {}: '{}'", ctx.method().toString(), ctx.path(), header);
            throw new TokenVerificationException("Authorization header is malformed");
        }
        return parts[1];
    }

    public UserDTO validateAndGetUserFromToken(Context ctx) throws TokenVerificationException {
        String token = getToken(ctx);
        UserDTO verifiedTokenUser = verifyToken(token);

        if (verifiedTokenUser == null) {
            logger.warn("Token verified but no user found at [{}] {}", ctx.method().toString(), ctx.path());
            throw new TokenVerificationException("Token is valid but user could not be resolved");
        }

        if (!dao.existingUsername(verifiedTokenUser.getUsername())) {
            logger.warn("Token references deleted user '{}' at [{}] {}", verifiedTokenUser.getUsername(), ctx.method(), ctx.path());
            throw new TokenVerificationException("Token is valid but user could not be resolved");
        }

        return verifiedTokenUser;
    }



    public boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        if (user == null || user.getRoles() == null || allowedRoles == null || allowedRoles.isEmpty()) {
            return false;
        }

        return user.getRoles().stream()
                .map(String::toUpperCase)
                .anyMatch(allowedRoles::contains);
    }
}
