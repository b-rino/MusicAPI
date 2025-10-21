package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.dtos.SongDTO;
import app.services.DeezerClient;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        ApplicationConfig.startServer(7074, emf);

    }
}
