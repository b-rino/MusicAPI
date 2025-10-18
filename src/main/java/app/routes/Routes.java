package app.routes;

import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {

    private final SecurityRoutes securityRoutes;

    public Routes(EntityManagerFactory emf){
        this.securityRoutes = new SecurityRoutes(emf);
    }

    public EndpointGroup getRoutes(){
        return () -> {
            get("/", ctx -> ctx.result("Hello World!"));
            path("auth", securityRoutes.getRoutes());
        } ;
    }
}
