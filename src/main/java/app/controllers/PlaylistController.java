package app.controllers;

import app.daos.UserDAO;
import app.dtos.AddSongDTO;
import app.dtos.CreatePlaylistDTO;
import app.dtos.PlaylistDTO;
import app.dtos.SongDTO;
import app.entities.Playlist;
import app.entities.User;
import app.exceptions.ValidationException;
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


    public void addSong(Context ctx) {
        int playlistId = Integer.parseInt(ctx.pathParam("id"));
        AddSongDTO dto = ctx.bodyAsClass(AddSongDTO.class);

        PlaylistDTO updated = service.addSong(playlistId, dto);
        ctx.status(200).json(updated);
    }

    public void getSongs(Context ctx) {
        int playlistId = Integer.parseInt(ctx.pathParam("id"));
        String username = SecurityUtils.getUsernameFromToken(ctx.header("Authorization").replace("Bearer ", ""));

        List<SongDTO> songs = service.getSongsForUserPlaylist(playlistId, username);
        ctx.json(songs);
    }



}
