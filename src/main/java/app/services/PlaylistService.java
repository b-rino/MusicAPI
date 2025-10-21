package app.services;

import app.daos.PlaylistDAO;
import app.dtos.PlaylistDTO;
import app.dtos.SongDTO;
import app.entities.Playlist;
import app.entities.User;
import app.exceptions.EntityAlreadyExistsException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaylistService {

    private final PlaylistDAO dao;

    public PlaylistService(PlaylistDAO dao){
        this.dao = dao;
    }

    public PlaylistDTO createPlaylist(String name, User owner){

        if(dao.existsByNameAndOwner(name, owner)){
            throw new EntityAlreadyExistsException("You already have a playlist with the name: '" + name + "'");
        }

        Playlist list = new Playlist();
        list.setName(name);
        list.setOwner(owner);

        Playlist saved = dao.create(list);

        return PlaylistDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .username(owner.getUsername())
                .songs(Set.of()) //tomt Set af sange ved oprettelse af ny playlist
                .build();
    }


    public List<PlaylistDTO> getPlaylistsForUser(String username) {
        List<Playlist> playlists = dao.getAllPlaylistsByOwner(username);
        return playlists.stream()
                .map(p -> PlaylistDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .username(username)
                        .songs(p.getSongs().stream().map(SongDTO::new).collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toList());
    }

}
