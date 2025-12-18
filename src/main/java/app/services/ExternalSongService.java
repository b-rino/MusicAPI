package app.services;

import app.dtos.SongDTO;
import app.exceptions.ApiException;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExternalSongService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ExternalSongService(){
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new Utils().getObjectMapper();
    }

    public List<SongDTO> searchSong(String query){
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.deezer.com/search?q=" + encodedQuery;

        String json = httpGet(url);
        try {
            var root = objectMapper.readTree(json);
            var data = root.get("data");

            List<SongDTO> songs = new ArrayList<>();

            for (var track : data) {
                SongDTO song = SongDTO.builder()
                        .externalId(track.get("id").asLong())
                        .title(track.get("title").asText())
                        .artist(track.get("artist").get("name").asText())
                        .album(track.get("album").get("title").asText())
                        .build();

                songs.add(song);
            }

            return songs;

        } catch (Exception e) {
            throw new ApiException("Failed to parse Deezer response");
        }
    }




    //performs a get request to the Deezer api (Open to ANYONE so no Authorization header)
    private String httpGet(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ApiException("Couldn't find song in API");
            }

            return response.body();
        } catch (URISyntaxException e) {
            throw new ApiException("Invalid URL");
        } catch (Exception e) {
            throw new ApiException("Failed to call Deezer API ");
        }
    }

    public SongDTO getSongByTrackId(long trackId) {
        String url = "https://api.deezer.com/track/" + trackId;

        String json = httpGet(url);
        try {
            var root = objectMapper.readTree(json);

            // Deezer returnerer et objekt, ikke et array
            if (root == null || root.get("id") == null) {
                throw new ApiException("No track found for id " + trackId);
            }

            return SongDTO.builder()
                    .externalId(root.get("id").asLong())
                    .title(root.get("title").asText())
                    .artist(root.get("artist").get("name").asText())
                    .album(root.get("album").get("title").asText())
                    .build();

        } catch (Exception e) {
            throw new ApiException("Failed to parse Deezer track response");
        }
    }

}
