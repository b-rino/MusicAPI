package app.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SongDTO {

    private String externalId;
    private String title;
    private String artist;
    private String album;
    private Integer releaseYear;

}
