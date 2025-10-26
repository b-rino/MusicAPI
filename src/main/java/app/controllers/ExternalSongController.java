package app.controllers;

import app.dtos.SongDTO;
import app.services.ExternalSongService;
import io.javalin.http.Context;

import java.util.List;

public class ExternalSongController {

    private final ExternalSongService externalSongService;

    public ExternalSongController(ExternalSongService ess){
        this.externalSongService = ess;
    }


    public void searchExternal(Context ctx) {
        String query = ctx.queryParam("query");
        if (query == null || query.isBlank()) {
            throw new IllegalStateException("Missing query parameter");
        }

        List<SongDTO> result = externalSongService.searchSong(query);

        ctx.status(200).json(result);
    }






}
