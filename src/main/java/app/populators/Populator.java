package app.populators;

import app.config.HibernateConfig;
import app.daos.AuthDAO;
import app.entities.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Populator {

    public static List<User> seededUsers = new ArrayList<>();
    public static List<Song> seededSongs = new ArrayList<>();
    public static List<Playlist> seededPlaylists = new ArrayList<>();

    public static void seedRoles(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(new Role("User"));
            em.persist(new Role("Admin"));
            em.getTransaction().commit();
        }
        System.out.println("Seeded roles: User + Admin");
    }

    public static void seedUsers(EntityManagerFactory emf) {
        AuthDAO dao = new AuthDAO(emf);
        seededUsers.clear();

        User user = dao.createUser("user1", "test123");
        dao.addUserRole(user.getUsername(), "User");

        User user2 = dao.createUser("user2", "test321");
        dao.addUserRole(user2.getUsername(), "User");

        User admin = dao.createUser("admin", "admin123");
        dao.addUserRole(admin.getUsername(), "Admin");

        seededUsers.addAll(List.of(user, user2, admin));
        System.out.println("Seeded users: " + seededUsers.stream().map(User::getUsername).collect(Collectors.toList()));
    }

    public static void seedSongs(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Song s1 = new Song(null, 1001, "Blinding Lights", "The Weeknd", "After Hours", new HashSet<>());
            Song s2 = new Song(null, 1002, "Levitating", "Dua Lipa", "Future Nostalgia", new HashSet<>());
            Song s3 = new Song(null, 1003, "Lose Yourself", "Eminem", "8 Mile", new HashSet<>());

            em.persist(s1);
            em.persist(s2);
            em.persist(s3);

            em.getTransaction().commit();
            seededSongs.addAll(List.of(s1, s2, s3));
        }
        System.out.println("Seeded songs: " + seededSongs.stream().map(Song::getTitle).collect(Collectors.toList()));
    }

    public static void seedPlaylists(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            User user = em.find(User.class, "user1");
            Playlist p1 = new Playlist();
            p1.setName("Workout");
            p1.setOwner(user);
            p1.getSongs().addAll(seededSongs.subList(0, 2)); //

            Playlist p2 = new Playlist();
            p2.setName("HippiliHipHop");
            p2.setOwner(user);
            p2.getSongs().add(seededSongs.get(2));

            em.persist(p1);
            em.persist(p2);

            em.getTransaction().commit();
            seededPlaylists.addAll(List.of(p1, p2));
        }
        System.out.println("Seeded playlists: " + seededPlaylists.stream().map(Playlist::getName).collect(Collectors.toList()));
    }

    public static void clearDatabase(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Playlist").executeUpdate();
            em.createQuery("DELETE FROM Song").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.getTransaction().commit();
        }
        seededUsers.clear();
        seededSongs.clear();
        seededPlaylists.clear();
        System.out.println("Cleared database.");
    }

   public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        clearDatabase(emf);
        seedRoles(emf);
        seedUsers(emf);
        seedSongs(emf);
        seedPlaylists(emf);
        System.out.println("Database seeded successfully!");
    }
}
