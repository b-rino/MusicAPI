package app.services;

import app.daos.SecurityDAO;
import app.dtos.UserDTO;
import app.entities.Role;
import app.entities.User;
import app.exceptions.EntityAlreadyExistsException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.TokenCreationException;
import app.exceptions.ValidationException;
import app.utils.SecurityUtils;
import app.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class AuthService {

    private final SecurityDAO dao;
    private final SecurityUtils securityUtils;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public AuthService(SecurityDAO dao, SecurityUtils securityUtils){
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
}
