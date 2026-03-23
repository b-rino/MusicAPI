package unit;

import app.daos.AuthDAO;
import app.dtos.UserDTO;
import app.entities.User;
import app.exceptions.EntityAlreadyExistsException;
import app.exceptions.UnauthorizedException;
import app.services.AuthService;
import app.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthDAO dao;
    private SecurityUtils securityUtils;
    private AuthService service;

    @BeforeEach
    void setUp() {
        dao = Mockito.mock(AuthDAO.class);
        securityUtils = Mockito.mock(SecurityUtils.class);
        service = new AuthService(dao, securityUtils);
    }

    @Test
    void getVerifiedUser_wrongCredentials_throwsUnauthorizedException() {
        when(dao.getVerifiedUser("user1", "wrongpass"))
                .thenThrow(new UnauthorizedException("Invalid username or password"));

        assertThrows(UnauthorizedException.class,
                () -> service.getVerifiedUser("user1", "wrongpass"));
    }

    @Test
    void getVerifiedUser_success_returnsUser() {
        User user = new User();
        user.setUsername("user1");

        when(dao.getVerifiedUser("user1", "pass")).thenReturn(user);

        User result = service.getVerifiedUser("user1", "pass");

        assertEquals("user1", result.getUsername());
    }

    @Test
    void register_existingUsername_throwsEntityAlreadyExistsException() {
        when(dao.existingUsername("user1")).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class,
                () -> service.register("user1", "pass"));
    }

    @Test
    void userHasAllowedRole_userHasRole_returnsTrue() {
        UserDTO user = new UserDTO("user1", Set.of("USER"));

        assertTrue(service.userHasAllowedRole(user, Set.of("USER")));
    }

    @Test
    void userHasAllowedRole_userLacksRole_returnsFalse() {
        UserDTO user = new UserDTO("user1", Set.of("USER"));

        assertFalse(service.userHasAllowedRole(user, Set.of("ADMIN")));
    }

    @Test
    void userHasAllowedRole_nullUser_returnsFalse() {
        assertFalse(service.userHasAllowedRole(null, Set.of("USER")));
    }

    @Test
    void userHasAllowedRole_caseInsensitive_returnsTrue() {
        UserDTO user = new UserDTO("user1", Set.of("user"));

        assertTrue(service.userHasAllowedRole(user, Set.of("USER")));
    }
}
