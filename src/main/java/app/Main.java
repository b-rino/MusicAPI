package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;

public class Main {
    public static void main(String[] args) {

        ApplicationConfig.startServer(7074);

        try {
            Thread.currentThread().join(); // Holder applikationen i gang
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
