package app.services;

import app.daos.SongDAO;
import app.daos.UserDAO;
import app.dtos.PlaylistDTO;
import app.dtos.SongDTO;
import app.dtos.UserDTO;
import app.entities.Role;
import app.entities.Song;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminService {

    private final UserDAO userDAO;
    private final SongDAO songDAO;

    public AdminService(UserDAO userDAO, SongDAO songDAO) {
        this.userDAO = userDAO;
        this.songDAO = songDAO;
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

    public void deleteUser(String targetUsername, String requesterUsername) {
        if (targetUsername.equals(requesterUsername)) {
            throw new ValidationException("Admins cannot delete themselves.");
        }

        User user = userDAO.findByUsername(targetUsername);
        if(user == null) throw new EntityNotFoundException("User with username: " + targetUsername + " was not found");

        userDAO.deleteUserByUsername(user.getUsername());
    }

    public List<SongDTO> getAllSongs() {
        List<Song> songs = songDAO.getAll();
        return songs.stream()
                .map(SongDTO::new)
                .collect(Collectors.toList());
    }


    public void grantRoleToUser(String username, String roleName) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new EntityNotFoundException("User not found: " + username);
        }


        Role role = userDAO.findByRoleName(roleName);

        user.addRole(role);

        userDAO.update(user);
    }



}

