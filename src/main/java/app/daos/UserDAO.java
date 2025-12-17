package app.daos;

import app.entities.Role;
import app.entities.Song;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;

public class UserDAO {

    EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<User> getAllUsers() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("FROM User", User.class).getResultList();
        }
    }

    public List<User> getAllUsersWithRolesAndPlaylists() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT u FROM User u " +
                            "LEFT JOIN FETCH u.roles " +
                            "LEFT JOIN FETCH u.playlists p " +
                            "LEFT JOIN FETCH p.songs",
                    User.class
            ).getResultList();
        }
    }

    //with eager role loading as well!
    public User findByUsername(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException("Couldn't find user in database");
        }
    }


    public void deleteUserByUsername(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();

            em.remove(user);
            em.getTransaction().commit();
        }
    }

    public void update(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        }
    }

    public Role findByRoleName(String roleName) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT r FROM Role r LEFT JOIN FETCH r.users WHERE r.roleName = :roleName", Role.class)
                    .setParameter("roleName", roleName)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException("The role you are trying to grant the user does not exist in system!");
        }
    }

    public User getUserDetails(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT DISTINCT u FROM User u " +
                                    "LEFT JOIN FETCH u.roles " +
                                    "LEFT JOIN FETCH u.playlists p " +
                                    "LEFT JOIN FETCH p.songs " +
                                    "WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException("Couldn't find user with username: " + username);
        }
    }




}
