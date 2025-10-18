package app.config;

import app.dtos.ErrorResponseDTO;
import app.enums.Role;
import app.exceptions.*;
import app.routes.Routes;
import app.routes.SecurityRoutes;
import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    public static Javalin startServer(int port, EntityManagerFactory emf) {
        Routes routes = new Routes(emf);
        SecurityRoutes securityRoutes = new SecurityRoutes(emf);
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);
            config.router.contextPath = "/api/v1";
            config.router.apiBuilder(routes.getRoutes());
        });

        //configureSecurity(app, securityRoutes.getSecurityController());
        configureLogging(app);
        configureExceptionHandling(app);

        app.start(port);
        return app;
    }

    public static void stopServer(Javalin app) {
        app.stop();
    }


    private static void configureExceptionHandling(Javalin app) {

        app.exception(IllegalStateException.class, (e, ctx) -> {
            logger.warn("Bad request at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(400).json(new ErrorResponseDTO(
                    "Invalid hotel or room data",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });

/*        app.exception(HotelNotFoundException.class, (e, ctx) -> {
            logger.warn("Handled HotelNotFoundException at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(404).json(new ErrorResponseDTO(
                    "Hotel not found",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });*/

        /*app.exception(RoomNotFoundException.class, (e, ctx) -> {
            logger.warn("Handled RoomNotFoundException at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(404).json(new ErrorResponseDTO(
                    "Room not found",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        }); */

        app.exception(ValidationException.class, (e, ctx) -> {
            logger.warn("Handled ValidationException at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(400).json(new ErrorResponseDTO(
                    "User not verified",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });

        app.exception(EntityNotFoundException.class, (e, ctx) -> {
            logger.warn("Handled EntityNotFoundException at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(404).json(new ErrorResponseDTO(
                    "Entity not found",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });

        app.exception(EntityAlreadyExistsException.class, (e, ctx) -> {
            logger.error("Handled EntityAlreadyExistsException at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(409).json(new ErrorResponseDTO(
                    "Entity already exists",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });

        app.exception(TokenVerificationException.class, (e, ctx) -> {
            logger.warn("Handled TokenVerificationException at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(401).json(new ErrorResponseDTO(
                    "Token verification failed",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });

        app.exception(TokenCreationException.class, (e, ctx) -> {
            logger.error("Handled TokenCreationException at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(500).json(new ErrorResponseDTO(
                    "Token creation failed",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });

        //Javalin exception, which I import and then override output to JSON! Default is status code 401 and no JSON (+ added logging)
        app.exception(UnauthorizedResponse.class, (e, ctx) -> {
            logger.warn("Handled UnauthorizedResponse at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(401).json(new ErrorResponseDTO(
                    "Unauthorized access",
                    e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });


        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unhandled exception at [{}] {}: {}", ctx.method(), ctx.path(), e.getMessage(), e);
            ctx.status(500).json(new ErrorResponseDTO(
                    "Internal server error",
                    e.getClass().getSimpleName() + " - " + e.getMessage(),
                    ctx.path(),
                    ctx.method().toString()
            ));
        });
    }


    private static void configureLogging(Javalin app) {
        app.before(ctx -> {
            logger.info("Incoming request: [{}] {} at {}", ctx.method(), ctx.path(), java.time.LocalDateTime.now());

            if (!ctx.body().isEmpty()) {
                String body = ctx.body();

                // Masking the password in the log-file
                String sanitizedBody = body.replaceAll("\"password\"\\s*:\\s*\".*?\"", "\"password\":\"***\"");
                logger.info("Request body: {}", sanitizedBody);
            }
        });


        app.after(ctx -> {
            logger.info("Response sent: [{}] {} at {}", ctx.status(), ctx.path(), java.time.LocalDateTime.now());
        });
    }

    /*public static void configureSecurity(Javalin app, SecurityController securityController) {
        app.beforeMatched(securityController.authenticate());
        app.beforeMatched(securityController.authorize());
    }*/
}
