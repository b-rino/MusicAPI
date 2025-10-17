package app.routes;

import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Routes {

    private final EntityManagerFactory emf;

    public Routes(EntityManagerFactory emf) {
        this.emf = emf;
    }



    public EndpointGroup getRoutes(){
        return () -> {
            get("/", ctx -> ctx.result("Hello World!"));
        } ;
    }
}
