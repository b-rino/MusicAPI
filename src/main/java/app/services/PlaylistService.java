package app.services;

import app.daos.PlaylistDAO;
import app.dtos.AddSongDTO;
import app.dtos.PlaylistDTO;
import app.dtos.SongDTO;
import app.entities.Playlist;
import app.entities.Song;
import app.entities.User;
import app.exceptions.EntityAlreadyExistsException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaylistService {

    private final PlaylistDAO dao;
    private final ExternalSongService externalSongService;

    public PlaylistService(PlaylistDAO dao,  ExternalSongService externalSongService) {
        this.externalSongService = externalSongService;
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


    public PlaylistDTO addSong(int playlistId, AddSongDTO dto, String username) {
        Playlist playlist = dao.getByIdWithOwner(playlistId);
        if (playlist == null || !playlist.getOwner().getUsername().equals(username)) {
            throw new ValidationException("You do not own this playlist");
        }

        Song song = new Song();
        song.setExternalId(dto.getExternalId());
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setAlbum(dto.getAlbum());

        Playlist updated = dao.addSongToPlaylist(playlistId, song);

        return PlaylistDTO.builder()
                .id(updated.getId())
                .name(updated.getName())
                .username(updated.getOwner().getUsername())
                .songs(updated.getSongs().stream().map(SongDTO::new).collect(Collectors.toSet()))
                .build();
    }


    public List<SongDTO> getSongsForPlaylist(int playlistId) {
        Set<Song> songs = dao.getSongsByPlaylistId(playlistId);
        return songs.stream().map(SongDTO::new).toList();
    }

    public List<SongDTO> getSongsForUserPlaylist(int playlistId, String username) {
        Playlist playlist = dao.getByIdWithOwner(playlistId);
        if (playlist == null || !playlist.getOwner().getUsername().equals(username)) {
            throw new ValidationException("You do not own this playlist");
        }
        return playlist.getSongs().stream().map(SongDTO::new).toList();
    }

    public void deletePlaylist(int playlistId, String username) {
        Playlist playlist = dao.getByIdWithOwner(playlistId);
        if (playlist == null) {
            throw new EntityNotFoundException("Playlist not found");
        }

        if (!playlist.getOwner().getUsername().equals(username)) {
            throw new ValidationException("You do not own this playlist");
        }

        dao.delete(playlistId);
    }

    public void removeSongFromPlaylist(int playlistId, int songId, String username) {
        Playlist playlist = dao.getByIdWithOwner(playlistId);
        if (playlist == null) {
            throw new EntityNotFoundException("Playlist not found");
        }

        if (!playlist.getOwner().getUsername().equals(username)) {
            throw new ValidationException("You do not own this playlist");
        }

        Song songToRemove = playlist.getSongs().stream()
                .filter(song -> song.getId() == songId)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Song not found in playlist"));

        playlist.removeSong(songToRemove);
        dao.update(playlist);
    }


    public PlaylistDTO updatePlaylistName(int playlistId, String newName, String username) {
        Playlist playlist = dao.getByIdWithOwner(playlistId);
        if (playlist == null) {
            throw new EntityNotFoundException("Playlist not found");
        }

        if (!playlist.getOwner().getUsername().equals(username)) {
            throw new ValidationException("You do not own this playlist");
        }

        playlist.setName(newName);
        Playlist updated = dao.update(playlist);

        return PlaylistDTO.builder()
                .id(updated.getId())
                .name(updated.getName())
                .username(updated.getOwner().getUsername())
                .songs(updated.getSongs().stream().map(SongDTO::new).collect(Collectors.toSet()))
                .build();
    }

    public PlaylistDTO addSongByExternalId(int playlistId, Long externalId, String username) {
        // 1. Hent playlist og check ejerskab
        Playlist playlist = dao.getByIdWithOwner(playlistId);
        if (playlist == null || !playlist.getOwner().getUsername().equals(username)) {
            throw new ValidationException("You do not own this playlist");
        }

        // 2. Slå sangen op via Deezer
        SongDTO externalSong = externalSongService.getSongByTrackId(externalId);
        if (externalSong == null) {
            throw new EntityNotFoundException("No song found with externalId " + externalId);
        }

        // 3. Opret Song‑entity baseret på Deezer‑metadata
        Song song = new Song();
        song.setExternalId(externalSong.getExternalId());
        song.setTitle(externalSong.getTitle());
        song.setArtist(externalSong.getArtist());
        song.setAlbum(externalSong.getAlbum());

        // 4. Brug DAO til at tilføje sangen til playlist
        Playlist updated = dao.addSongToPlaylist(playlistId, song);

        // 5. Returner DTO
        return PlaylistDTO.builder()
                .id(updated.getId())
                .name(updated.getName())
                .username(updated.getOwner().getUsername())
                .songs(updated.getSongs().stream()
                        .map(SongDTO::new)
                        .collect(Collectors.toSet()))
                .build();
    }

}
