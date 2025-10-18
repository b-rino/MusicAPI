package app.routes;

import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Routes {

    public EndpointGroup getRoutes(){
        return () -> {
            get("/", ctx -> ctx.result("Hello World!"));
            get("healthcheck", ctx -> ctx.status(200).json(Map.of("msg", "API is up and running")));
        } ;
    }
}
