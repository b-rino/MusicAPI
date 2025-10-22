package app.services;

import app.daos.UserDAO;
import app.dtos.PlaylistDTO;
import app.dtos.UserDTO;
import app.entities.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminService {

    private final UserDAO userDAO;

    public AdminService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userDAO.getAllUsersWithRolesAndPlaylists();

        return users.stream()
                .map(user -> {
                    Set<String> roles = user.getRoles().stream()
                            .map(role -> role.getRoleName())
                            .collect(Collectors.toSet());

                    Set<PlaylistDTO> playlists = user.getPlaylists().stream()
                            .map(PlaylistDTO::new)
                            .collect(Collectors.toSet());

                    return UserDTO.builder()
                            .username(user.getUsername())
                            .roles(roles)
                            .playlists(playlists)
                            .build();
                })
                .collect(Collectors.toList());
    }

}

