package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true)
    private Integer id;

    @Column(name = "external_id", unique = true, nullable = false)
    private Integer externalId;

    @ToString.Include
    @Column(length = 100, nullable = false)
    private String title;
    @ToString.Include
    @Column(length = 100, nullable = false)
    private String artist;
    @ToString.Include
    @Column(length = 50)
    private String album;
/*    @ToString.Include         external API doesn't have release year!?
    private Integer releaseYear;*/

    @ManyToMany(mappedBy = "songs")
    private Set<Playlist> playlists = new HashSet();

}
