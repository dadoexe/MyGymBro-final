package com.example.mygymbro.dao;

import com.example.mygymbro.model.Exercise;
import com.example.mygymbro.model.MuscleGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestApiExerciseDAO implements ExerciseDAO {

    // 1. Aggiungiamo il Logger
    private static final Logger logger = Logger.getLogger(RestApiExerciseDAO.class.getName());

    private static final String API_URL = "https://exercisedb.p.rapidapi.com/exercises?offset=0&limit=50";
    // 2. Prendiamo la chiave dalle variabili d'ambiente (Sicuro!)
    private static final String API_KEY = System.getenv("RAPID_API_KEY");
    private static final String API_HOST = "exercisedb.p.rapidapi.com";

    @Override
    public List<Exercise> findAll() {
        List<Exercise> modelList = new ArrayList<>();

        try {
            // 3. Log invece di System.out
            logger.log(Level.INFO, "VERIFICA URL: Sto chiamando questo indirizzo: {0}", API_URL);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("X-RapidAPI-Key", API_KEY)
                    .header("X-RapidAPI-Host", API_HOST)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                parseResponse(response.body(), modelList);
            } else {
                // 4. Log errore invece di System.err
                logger.log(Level.SEVERE, "Errore API nella findAll: Codice {0}", response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            // 5. Log eccezione invece di printStackTrace
            logger.log(Level.SEVERE, "Eccezione durante la chiamata API findAll", e);
            Thread.currentThread().interrupt(); // Buona pratica quando si cattura InterruptedException
        }

        return modelList;
    }


    public List<Exercise> search(String keyword) {
        List<Exercise> modelList = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        // Costruiamo l'URL
        String searchUrl = "https://exercisedb.p.rapidapi.com/exercises/name/" + keyword.trim().replace(" ", "%20") + "?limit=50";

        try {
            logger.log(Level.INFO, "RICERCA LIVE: {0}", searchUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    // ⚠️ QUI C'ERA L'ERRORE: Ora usiamo le costanti sicure!
                    .header("X-RapidAPI-Key", API_KEY)
                    .header("X-RapidAPI-Host", API_HOST)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                parseResponse(response.body(), modelList);
            } else {
                logger.log(Level.WARNING, "Errore API nella search: Codice {0}", response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Eccezione durante la ricerca API", e);

            // Buona pratica: se è un errore di interruzione, ripristina il flag
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }

        return modelList;
    }

    // Metodo helper per evitare duplicazione codice nel parsing
    private void parseResponse(String jsonBody, List<Exercise> modelList) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ApiExerciseDto>>() {}.getType();
        List<ApiExerciseDto> apiList = gson.fromJson(jsonBody, listType);

        if (apiList != null) {
            for (ApiExerciseDto dto : apiList) {
                int fakeId = (dto.id != null) ? dto.id.hashCode() : 0;
                String description = (dto.instructions != null) ? String.join(" ", dto.instructions) : "Descrizione non disponibile";
                MuscleGroup mg = mapApiBodyPartToEnum(dto.bodyPart);

                Exercise model = new Exercise(fakeId, dto.name, description, mg);
                model.setGifUrl(dto.gifUrl);
                modelList.add(model);
            }
        }
    }

    private MuscleGroup mapApiBodyPartToEnum(String apiBodyPart) {
        if (apiBodyPart == null) return MuscleGroup.CHEST;

        // Sintassi moderna: casi multipli sulla stessa riga separati da virgola
        switch (apiBodyPart.toLowerCase().trim()) {
            case "chest":
                return MuscleGroup.CHEST;
            case "back":
                return MuscleGroup.BACK;
            case "shoulders", "neck": // Raggruppati!
                return MuscleGroup.SHOULDERS;
            case "upper arms", "lower arms": // Raggruppati!
                return MuscleGroup.ARMS;
            case "upper legs", "lower legs": // Raggruppati!
                return MuscleGroup.LEGS;
            case "waist":
                return MuscleGroup.ABS;
            case "cardio":
                return MuscleGroup.CARDIO;
            default:
                logger.log(Level.WARNING, "Gruppo muscolare sconosciuto: {0} -> Mappato su CHEST", apiBodyPart);
                return MuscleGroup.CHEST;
        }
    }

    @Override
    public Exercise findByName(String name) {
        return findAll().stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private class ApiExerciseDto {
        String bodyPart;
        String equipment;
        String gifUrl;
        String id;
        String name;
        String target;
        List<String> instructions;
    }
}
