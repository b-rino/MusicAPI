package app.dtos;

import app.entities.Song;
import app.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistDTO {

    private Integer id;
    private String name;
    private String ownerUsername;
    private Set<SongDTO> songs;

}
