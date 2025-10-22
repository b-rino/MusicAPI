package app.daos;

import app.config.HibernateConfig;
import app.entities.Playlist;
import app.entities.Role;
import app.entities.User;
import app.exceptions.EntityAlreadyExistsException;
import app.exceptions.EntityNotFoundException;
import app.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class AuthDAO {

    EntityManagerFactory emf;

    public AuthDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }


    public User getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username", User.class);
            query.setParameter("username", username);

            User foundUser = query.getSingleResult();

            if (foundUser.checkPassword(password)) {
                return foundUser;
            } else {
                throw new ValidationException("Invalid username or password");
            }
        } catch (NoResultException e) {
            throw new ValidationException("User not found");
        }
    }



    public User createUser(String username, String password) throws EntityAlreadyExistsException {
        try (EntityManager em = emf.createEntityManager()) {
            User existing = em.find(User.class, username);
            if (existing == null) {
                User newUser = new User(username, password);
                em.getTransaction().begin();
                em.persist(new User(username, password));
                em.getTransaction().commit();
                return newUser;
            } else throw new EntityAlreadyExistsException("Username already exists");
        }
    }


    public User addUserRole(String username, String rolename) throws EntityNotFoundException {
        try (EntityManager em = emf.createEntityManager()) {
            User user = em.find(User.class, username);
            Role role = em.find(Role.class, rolename);
            if (user == null || role == null) {
                throw new EntityNotFoundException("User or role does not exist!");
            }
            em.getTransaction().begin();
            user.addRole(role);
            em.getTransaction().commit();
            return user;
        }
    }

    public User addUserPlaylist(String username, int id) throws EntityNotFoundException{
        try (EntityManager em = emf.createEntityManager()){
            User user = em.find(User.class, username);
            Playlist playlist = em.find(Playlist.class, id);
            if(user == null || playlist == null){
                throw new EntityNotFoundException("User or playlist not found!");
            }
            em.getTransaction().begin();
            user.addPlaylist(playlist);
            em.getTransaction().commit();
            return user;
        }
    }

    public Role createRole(String roleName) throws EntityAlreadyExistsException {
        try (EntityManager em = emf.createEntityManager()) {
            Role role = em.find(Role.class, roleName);
            if (role == null) {
                role = new Role(roleName);
                em.getTransaction().begin();
                em.persist(role);
                em.getTransaction().commit();
                return role;
            } else {
                throw new EntityAlreadyExistsException("Role already exists");
            }
        }
    }






    //TODO: Bliver denne metode brugt i sidste ende?
    public boolean existingUsername(String username) throws EntityAlreadyExistsException {
        try(EntityManager em = emf.createEntityManager()){
            User user = em.find(User.class, username);
            if(user != null){
                return true;
            } else {
                return false;
            }
        }
    }


    public static void main(String[] args) {
        AuthDAO dao = new AuthDAO(HibernateConfig.getEntityManagerFactory());

        dao.createUser("admin", "admin");
        dao.createUser("User", "User");
        dao.createRole("User");
        dao.createRole("Admin");
        dao.addUserRole("admin", "Admin");
        dao.addUserRole("User", "User");
    }
}
