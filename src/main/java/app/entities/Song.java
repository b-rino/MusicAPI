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

    @ToString.Include
    @Column(length = 100, nullable = false)
    private String title;
    @ToString.Include
    @Column(length = 100, nullable = false)
    private String artist;
    @ToString.Include
    @Column(length = 50, nullable = true)
    private String album;
    @ToString.Include
    private Integer releaseYear;

    @ManyToMany(mappedBy = "songs")
    private Set<Playlist> playlists = new HashSet();



}
