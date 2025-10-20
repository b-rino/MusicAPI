package app.entities;

import jakarta.persistence.*;
import lombok.*;
import org.mindrot.jbcrypt.BCrypt;


import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @Column(name = "username", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String username;
    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_name"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    private Set<Role> roles = new HashSet();

    @OneToMany(mappedBy = "owner")
    private Set<Playlist> playlists = new HashSet();


    public User(String username, String password){
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.username = username;
    }

    public boolean checkPassword(String realPassword) {
        if(BCrypt.checkpw(realPassword, password)) {
            return true;
        } else {
            return false;
        }
    }


    public void addRole(Role role){
        roles.add(role);
        role.getUsers().add(this);
    }

    public void addPlaylist(Playlist playlist){
        playlists.add(playlist);
        playlist.setOwner(this);
    }
}
