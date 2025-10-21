package app.daos;

import app.config.HibernateConfig;
import app.entities.Playlist;
import app.entities.Song;
import app.entities.User;
import app.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Set;

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

    public boolean existsByNameAndOwner(String name, User owner) {
        try (EntityManager em = emf.createEntityManager()) {
            Long count = em.createQuery(
                            "SELECT COUNT(p) FROM Playlist p WHERE p.name = :name AND p.owner = :owner", Long.class)
                    .setParameter("name", name)
                    .setParameter("owner", owner)
                    .getSingleResult();
            return count > 0;
        }
    }

    public List<Playlist> getAllPlaylistsByOwner(String username){
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("SELECT DISTINCT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.owner.username = :username", Playlist.class)
                    .setParameter("username", username)
                    .getResultList();
        }
    }

    public Playlist addSongToPlaylist(int playlistId, Song song) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Playlist playlist = em.find(Playlist.class, playlistId);
            if (playlist == null) {
                throw new EntityNotFoundException("Playlist not found");
            }


            TypedQuery<Song> query = em.createQuery(
                    "SELECT s FROM Song s WHERE s.externalId = :externalId", Song.class);
            query.setParameter("externalId", song.getExternalId());
            List<Song> existing = query.getResultList();

            Song songToAdd = existing.isEmpty() ? song : existing.get(0);
            playlist.getSongs().add(songToAdd);

            if (existing.isEmpty()) {
                em.persist(songToAdd);
            }

            Playlist updatedList = em.merge(playlist);
            em.getTransaction().commit();
            return updatedList;
        }
    }


    public Set<Song> getSongsByPlaylistId(int playlistId) {
        try (EntityManager em = emf.createEntityManager()) {
            Playlist playlist = em.createQuery(
                            "SELECT p FROM Playlist p LEFT JOIN FETCH p.songs WHERE p.id = :id", Playlist.class)
                    .setParameter("id", playlistId)
                    .getSingleResult();

            return playlist.getSongs();
        }
    }


    public Playlist getByIdWithOwner(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT p FROM Playlist p LEFT JOIN FETCH p.songs LEFT JOIN FETCH p.owner WHERE p.id = :id",
                            Playlist.class)
                    .setParameter("id", id)
                    .getSingleResult();
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
