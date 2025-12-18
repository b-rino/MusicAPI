package app.controllers;

import app.dtos.SongDTO;
import app.exceptions.EntityNotFoundException;
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


    public void searchByTrackId(Context ctx) {
        String trackIdParam = ctx.pathParam("trackId");

        if (trackIdParam == null || trackIdParam.isBlank()) {
            throw new IllegalStateException("Missing track id parameter");
        }

        long trackId;
        try {
            trackId = Long.parseLong(trackIdParam);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid track id parameter - must be a number");
        }

        if (trackId <= 0) {
            throw new IllegalStateException("Invalid track id parameter - it must be a positive number");
        }

        SongDTO song = externalSongService.getSongByTrackId(trackId);
        if (song == null) {
            throw new EntityNotFoundException("Song not found with id " + trackId);
        }

        ctx.json(song);
    }

}
