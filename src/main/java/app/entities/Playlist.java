package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(length = 100)
    private String name;

    @ManyToOne
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Song> songs = new HashSet();



    public void removeSong(Song song) {
        if (songs != null) {
            songs.remove(song);
            song.setPlaylists(null); // for breaking bi directional link
        }
    }

}
