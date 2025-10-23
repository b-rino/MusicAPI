package app.routes;

import app.enums.Role;
import app.services.DeezerClient;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {

    private final AuthRoutes authRoutes;
    private final SystemRoutes systemRoutes;
    private final PlaylistRoutes playlistRoutes;
    private final AdminRoutes adminRoutes;

    public Routes(EntityManagerFactory emf){
        DeezerClient dc = new DeezerClient();
        this.authRoutes = new AuthRoutes(emf);
        this.systemRoutes = new SystemRoutes(dc);
        this.playlistRoutes = new PlaylistRoutes(emf);
        this.adminRoutes = new AdminRoutes(emf);
    }

    public EndpointGroup getRoutes(){
        return () -> {
            get("/", ctx -> ctx.result("Welcome to the MusicAPI. Please visit 'music.brino.dk/api/v1routes' to see available routes"));
            path("", systemRoutes.getRoutes());
            path("", authRoutes.getRoutes());
            path("", playlistRoutes.getRoutes());
            path("admin", adminRoutes.getRoutes()); //Da det kun er for admin's bryder jeg med den "normale" struktur
        } ;
    }
}