package app.dtos;


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

}
