package unit;

import app.daos.PlaylistDAO;
import app.dtos.AddSongDTO;
import app.dtos.PlaylistDTO;
import app.entities.Playlist;
import app.entities.Song;
import app.entities.User;
import app.exceptions.EntityAlreadyExistsException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import app.services.ExternalSongService;
import app.services.PlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaylistServiceTest {

    private PlaylistDAO dao;
    private ExternalSongService externalSongService;
    private PlaylistService service;

    private User owner;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        dao = Mockito.mock(PlaylistDAO.class);
        externalSongService = Mockito.mock(ExternalSongService.class);
        service = new PlaylistService(dao, externalSongService);

        owner = new User();
        owner.setUsername("testuser");

        playlist = new Playlist();
        playlist.setId(1);
        playlist.setName("My Playlist");
        playlist.setOwner(owner);
        playlist.setSongs(new HashSet<>());
    }

    @Test
    void createPlaylist_success() {
        when(dao.existsByNameAndOwner("My Playlist", owner)).thenReturn(false);
        when(dao.create(any(Playlist.class))).thenReturn(playlist);

        PlaylistDTO result = service.createPlaylist("My Playlist", owner);

        assertEquals("My Playlist", result.getName());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void createPlaylist_duplicateName_throwsEntityAlreadyExistsException() {
        when(dao.existsByNameAndOwner("My Playlist", owner)).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class,
                () -> service.createPlaylist("My Playlist", owner));
    }

    @Test
    void addSong_success() {
        AddSongDTO dto = new AddSongDTO();
        dto.setTitle("Song Title");
        dto.setArtist("Artist");
        dto.setAlbum("Album");
        dto.setExternalId(123L);

        when(dao.getByIdWithOwner(1)).thenReturn(playlist);
        when(dao.addSongToPlaylist(eq(1), any(Song.class))).thenReturn(playlist);

        PlaylistDTO result = service.addSong(1, dto, "testuser");

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void addSong_notOwner_throwsValidationException() {
        when(dao.getByIdWithOwner(1)).thenReturn(playlist);

        AddSongDTO dto = new AddSongDTO();
        assertThrows(ValidationException.class,
                () -> service.addSong(1, dto, "otheruser"));
    }

    @Test
    void addSong_playlistNotFound_throwsEntityNotFoundException() {
        when(dao.getByIdWithOwner(99)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.addSong(99, new AddSongDTO(), "testuser"));
    }

    @Test
    void deletePlaylist_success() {
        when(dao.getByIdWithOwner(1)).thenReturn(playlist);

        assertDoesNotThrow(() -> service.deletePlaylist(1, "testuser"));
        verify(dao).delete(1);
    }

    @Test
    void deletePlaylist_notOwner_throwsValidationException() {
        when(dao.getByIdWithOwner(1)).thenReturn(playlist);

        assertThrows(ValidationException.class,
                () -> service.deletePlaylist(1, "otheruser"));
        verify(dao, never()).delete(anyInt());
    }

    @Test
    void deletePlaylist_notFound_throwsEntityNotFoundException() {
        when(dao.getByIdWithOwner(99)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.deletePlaylist(99, "testuser"));
    }

    @Test
    void updatePlaylistName_success() {
        when(dao.getByIdWithOwner(1)).thenReturn(playlist);
        when(dao.update(playlist)).thenReturn(playlist);

        PlaylistDTO result = service.updatePlaylistName(1, "New Name", "testuser");

        assertEquals("New Name", playlist.getName());
        assertNotNull(result);
    }

    @Test
    void updatePlaylistName_notOwner_throwsValidationException() {
        when(dao.getByIdWithOwner(1)).thenReturn(playlist);

        assertThrows(ValidationException.class,
                () -> service.updatePlaylistName(1, "New Name", "otheruser"));
    }

    @Test
    void getPlaylistsForUser_returnsEmptyList() {
        when(dao.getAllPlaylistsByOwner("testuser")).thenReturn(List.of());

        List<PlaylistDTO> result = service.getPlaylistsForUser("testuser");

        assertTrue(result.isEmpty());
    }
}
