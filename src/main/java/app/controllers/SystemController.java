package app.controllers;

import app.dtos.SongDTO;
import app.services.DeezerClient;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

public class SystemController {

    private final DeezerClient deezerClient;

    public SystemController(DeezerClient dc){
        this.deezerClient = dc;
    }

    public void healthCheck(Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }


    public void searchExternal(Context ctx) {
        String query = ctx.queryParam("query");
        if (query == null || query.isBlank()) {
            throw new IllegalStateException("Missing query parameter");
        }

        List<SongDTO> result = deezerClient.searchSong(query);

        ctx.status(200).json(result);
    }


}
