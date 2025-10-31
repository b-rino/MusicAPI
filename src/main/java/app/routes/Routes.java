package app.routes;

import app.services.ExternalSongService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {

    private final AuthRoutes authRoutes;
    private final ExternalSongRoutes externalSongRoutes;
    private final PlaylistRoutes playlistRoutes;
    private final AdminRoutes adminRoutes;

    public Routes(EntityManagerFactory emf){
        ExternalSongService ess = new ExternalSongService();
        this.authRoutes = new AuthRoutes(emf);
        this.externalSongRoutes = new ExternalSongRoutes(ess);
        this.playlistRoutes = new PlaylistRoutes(emf);
        this.adminRoutes = new AdminRoutes(emf);
    }

    public EndpointGroup getRoutes(){
        return () -> {
            get("/", ctx -> ctx.result("Welcome to the MusicAPI. Please visit 'music.brino.dk/api/v1routes' to see available routes"));
            get("/healthcheck", ctx -> ctx.result("API is up and running"));
            path("", externalSongRoutes.getRoutes());
            path("", authRoutes.getRoutes());
            path("", playlistRoutes.getRoutes());
            path("admin", adminRoutes.getRoutes()); //Da det kun er for admin's bryder jeg med den "normale" struktur
        } ;
    }
}