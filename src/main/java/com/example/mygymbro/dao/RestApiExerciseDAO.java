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

public class RestApiExerciseDAO implements ExerciseDAO {


    private static final String API_URL = "https://exercisedb.p.rapidapi.com/exercises?offset=0&limit=50";
    private static final String API_KEY = "b5a76e4d57msh6edf21dcd3dd851p199802jsne8b14218cbd4";
    private static final String API_HOST = "exercisedb.p.rapidapi.com";

    @Override
    public List<Exercise> findAll() {
        List<Exercise> modelList = new ArrayList<>();

        try {
            System.out.println("VERIFICA URL: Sto chiamando questo indirizzo: " + API_URL);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("X-RapidAPI-Key", API_KEY)
                    .header("X-RapidAPI-Host", API_HOST)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String jsonBody = response.body();
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ApiExerciseDto>>() {}.getType();
                List<ApiExerciseDto> apiList = gson.fromJson(jsonBody, listType);

                if (apiList != null) {
                    for (ApiExerciseDto dto : apiList) {
                        int fakeId = (dto.id != null) ? dto.id.hashCode() : 0;
                        String description = (dto.instructions != null) ? String.join(" ", dto.instructions) : "Nessuna descrizione";

                        // --- MODIFICA INIZIO: Mappatura intelligente ---
                        MuscleGroup mg = mapApiBodyPartToEnum(dto.bodyPart);
                        // --- MODIFICA FINE ---

                        Exercise model = new Exercise(fakeId, dto.name, description, mg);
                        model.setGifUrl(dto.gifUrl);
                        modelList.add(model);
                    }
                }
            } else {
                System.err.println("Errore API: Codice " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return modelList;
    }


    public List<Exercise> search(String keyword) {
        List<Exercise> modelList = new ArrayList<>();

        // Se la ricerca è vuota, restituiamo i "consigliati" (la findAll standard)
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        // COSTRUIAMO L'URL DI RICERCA SPECIFICO

        String searchUrl = "https://exercisedb.p.rapidapi.com/exercises/name/" + keyword.trim().replace(" ", "%20") + "?limit=50";

        try {
            System.out.println("RICERCA LIVE: " + searchUrl); // Debug

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(searchUrl))
                    .header("X-RapidAPI-Key", "b5a76e4d57msh6edf21dcd3dd851p199802jsne8b14218cbd4") // Usa la tua costante API_KEY
                    .header("X-RapidAPI-Host", "exercisedb.p.rapidapi.com") // Usa la tua costante API_HOST
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {


                String jsonBody = response.body();
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<ApiExerciseDto>>() {}.getType();
                List<ApiExerciseDto> apiList = gson.fromJson(jsonBody, listType);

                if (apiList != null) {
                    for (ApiExerciseDto dto : apiList) {
                        int fakeId = (dto.id != null) ? dto.id.hashCode() : 0;
                        String description = (dto.instructions != null) ? String.join(" ", dto.instructions) : "Descrizione...";
                        com.example.mygymbro.model.MuscleGroup mg = mapApiBodyPartToEnum(dto.bodyPart);
                        modelList.add(new Exercise(fakeId, dto.name, description, mg));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelList;
    }

    /**
     * Metodo Helper per convertire le stringhe dell'API (es. "waist", "upper legs")
     * nei nostri Enum Java.
     */
    private MuscleGroup mapApiBodyPartToEnum(String apiBodyPart) {
        if (apiBodyPart == null) return MuscleGroup.CHEST; // Default

        // Normalizziamo la stringa (minuscolo) per il confronto
        switch (apiBodyPart.toLowerCase().trim()) {
            case "chest":           return MuscleGroup.CHEST;
            case "back":            return MuscleGroup.BACK;
            case "shoulders":       return MuscleGroup.SHOULDERS;

            // Mappiamo le varie parti delle braccia su ARMS
            case "upper arms":
            case "lower arms":      return MuscleGroup.ARMS;

            // Mappiamo le varie parti delle gambe su LEGS
            case "upper legs":
            case "lower legs":      return MuscleGroup.LEGS;

            // Il famoso fix per "waist"
            case "waist":           return MuscleGroup.ABS;

            case "cardio":          return MuscleGroup.CARDIO;
            case "neck":            return MuscleGroup.SHOULDERS; // O altro se preferisci

            default:
                // Logghiamo solo se troviamo qualcosa di davvero nuovo
                System.out.println("Gruppo muscolare sconosciuto: " + apiBodyPart + " -> Mappato su CHEST");
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
