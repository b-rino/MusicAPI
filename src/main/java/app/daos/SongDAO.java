package app.daos;

import app.config.HibernateConfig;
import app.entities.Song;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class SongDAO implements IDAO<Song, Integer>{

    private final EntityManagerFactory emf;

    public SongDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public Song create(Song song) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(song);
            em.getTransaction().commit();
            return song;
        }
    }

    @Override
    public Song getById(Integer id) {
        try(EntityManager em = emf.createEntityManager()){
            return em.find(Song.class, id);
        }
    }

    @Override
    public Song update(Song song) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Song updatedSong = em.merge(song);
            em.getTransaction().commit();
            return updatedSong;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Song songToDelete = em.find(Song.class, id);
            if(songToDelete == null){
                em.getTransaction().rollback();
                return false;
            }
            em.remove(songToDelete);
            em.getTransaction().commit();
            return true;
        }
    }

    @Override
    public List<Song> getAll() {
        try(EntityManager em = emf.createEntityManager()){
           return em.createQuery("FROM Song", Song.class).getResultList();
        }
    }


    //TODO: Slet denne
    public static void main(String[] args) {
        SongDAO dao = new SongDAO(HibernateConfig.getEntityManagerFactory());

        Song testSong = new Song();
        testSong.setTitle("Test Track");
        testSong.setArtist("Test Artist");
        testSong.setAlbum("Test Album");
        testSong.setReleaseYear(2025);

        Song savedSong = dao.create(testSong);
        System.out.println("Saved song" + savedSong.getId());

        List<Song> allSong = dao.getAll();
        System.out.println(allSong);

        Song id1 = dao.getById(1);
        System.out.println(id1);

        dao.delete(1);
        System.out.println("deleted all");
        System.out.println(dao.getAll());
    }
}
