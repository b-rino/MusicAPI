package app.controllers;

import app.daos.UserDAO;
import app.dtos.CreatePlaylistDTO;
import app.dtos.PlaylistDTO;
import app.entities.User;
import app.services.PlaylistService;
import app.utils.SecurityUtils;
import io.javalin.http.Context;

import java.util.List;

public class PlaylistController {

    private final PlaylistService service;
    private final UserDAO dao;

    public PlaylistController(PlaylistService service, UserDAO dao){
        this.service = service;
        this.dao = dao;
    }

    public void create(Context ctx){
        CreatePlaylistDTO dto = ctx.bodyAsClass(CreatePlaylistDTO.class);

        if(dto.getName() == null || dto.getName().isBlank()){
            throw new IllegalArgumentException("Playlist name is required");
        }

        String token = ctx.header("Authorization").replace("Bearer", "");
        String username = SecurityUtils.getUsernameFromToken(token);

        User user = dao.findByUsername(username);

        PlaylistDTO created = service.createPlaylist(dto.getName(), user);
        ctx.status(201).json(created);

    }


    public void getAllPlaylistsForUser(Context ctx){
        String token = ctx.header("Authorization").replace("Bearer", "");
        String username = SecurityUtils.getUsernameFromToken(token);


        List<PlaylistDTO> playlists = service.getPlaylistsForUser(username);
        ctx.json(playlists);

    }
}
