package app.services;

import app.dtos.SongDTO;
import app.exceptions.ApiException;
import app.utils.SecurityUtils;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class DeezerClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeezerClient(){
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new Utils().getObjectMapper();
    }

    public List<SongDTO> searchSong(String query){
        String url = "https://api.deezer.com/search?q=" + query;

        String json = httpGet(url);
        try {
            var root = objectMapper.readTree(json);
            var data = root.get("data");

            List<SongDTO> songs = new ArrayList<>();

            for (var track : data) {
                SongDTO song = SongDTO.builder()
                        .externalId(track.get("id").asInt())
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





    //performs an authenticated get request to the Deezer api
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

}
