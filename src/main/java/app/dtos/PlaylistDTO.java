package app.dtos;

import app.entities.Playlist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistDTO {

    private Integer id;
    private String name;
    private String username;
    private Set<SongDTO> songs;

    public PlaylistDTO(Playlist playlist) {
        this.id = playlist.getId();
        this.name = playlist.getName();
        this.username = playlist.getOwner().getUsername();
        this.songs = playlist.getSongs().stream()
                .map(SongDTO::new)
                .collect(Collectors.toSet());
    }


}
