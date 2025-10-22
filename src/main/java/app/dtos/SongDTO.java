package app.dtos;


import app.entities.Song;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SongDTO {

    private Integer id;
    private Integer externalId;
    private String title;
    private String artist;
    private String album;



    public SongDTO(Song song) {
        this.id = song.getId();
        this.externalId = song.getExternalId();
        this.title = song.getTitle();
        this.artist = song.getArtist();
        this.album = song.getAlbum();
    }


}
