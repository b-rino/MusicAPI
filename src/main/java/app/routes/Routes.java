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

    public Routes(EntityManagerFactory emf){
        DeezerClient dc = new DeezerClient();
        this.authRoutes = new AuthRoutes(emf);
        this.systemRoutes = new SystemRoutes(dc);
        this.playlistRoutes = new PlaylistRoutes(emf);
    }

    public EndpointGroup getRoutes(){
        return () -> {
            get("/", ctx -> ctx.result("Hello World!"));
            path("", systemRoutes.getRoutes());
            path("", authRoutes.getRoutes());
            path("", playlistRoutes.getRoutes());
        } ;
    }
}






/*            get("/users", ctx ->{
                List<UserDTO> users = userService.getAllUsers();
                ctx.json(users);
            } , Role.ADMIN);*/