package app.daos;

import app.config.HibernateConfig;
import app.entities.Playlist;
import app.entities.Song;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class PlaylistDAO implements IDAO <Playlist, Integer> {

    private EntityManagerFactory emf;

    public PlaylistDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public Playlist create(Playlist playlist) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(playlist);
            em.getTransaction().commit();
            return playlist;
        }
    }

    @Override
    public Playlist getById(Integer id) {
        try(EntityManager em = emf.createEntityManager()){
            return em.find(Playlist.class, id);
        }
    }

    @Override
    public Playlist update(Playlist playlist) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Playlist updatedPlaylist = em.merge(playlist);
            em.getTransaction().commit();
            return updatedPlaylist;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Playlist playlistToDelete = em.find(Playlist.class, id);
            if(playlistToDelete == null){
                em.getTransaction().rollback();
                return false;
            }
            em.remove(playlistToDelete);
            em.getTransaction().commit();
            return true;
        }
    }

    @Override
    public List<Playlist> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("FROM Playlist", Playlist.class).getResultList();
        }
    }

    public static void main(String[] args) {
        PlaylistDAO dao = new PlaylistDAO(HibernateConfig.getEntityManagerFactory());
        SecurityDAO sdao = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        SongDAO songDAO = new SongDAO(HibernateConfig.getEntityManagerFactory());



        Song song1 = new Song();
        song1.setTitle("Min sang");
        song1.setArtist("B-rabbit");
        song1.setAlbum("B-rabbit Deluxe edition");
        songDAO.create(song1);

        Song song2 = new Song();
        song2.setTitle("Din sang");
        song2.setArtist("K-rabbit");
        song2.setAlbum("K-rabbit Deluxe edition");
        songDAO.create(song2);

        Playlist list = new Playlist();
        list.setName("Min Liste");
        list.getSongs().add(song1);
        list.getSongs().add(song2);
        dao.create(list);




        User mig = sdao.getVerifiedUser("Benjamino6", "1234");
        System.out.println("Bruger med rolle: " + mig);
        User userWithRoleAndPlaylist = sdao.addUserPlaylist(mig.getUsername(), list.getId());
        System.out.println("Bruger med playlist: " + userWithRoleAndPlaylist);



    }
}
