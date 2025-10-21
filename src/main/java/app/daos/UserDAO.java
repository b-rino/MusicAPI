package app.daos;

import app.entities.Song;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class UserDAO {

    EntityManagerFactory emf;
    public UserDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    public List<User> getAllUseers() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("FROM User", User.class).getResultList();
        }
    }
}
