package com.example.mygymbro;

import com.example.mygymbro.dao.RestApiExerciseDAO;
import com.example.mygymbro.model.Exercise;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExerciseApiTest {

    @Test
    void testFetchExercisesFromApi() {
        // 1. Setup: Istanzio il DAO reale
        RestApiExerciseDAO dao = new RestApiExerciseDAO();

        // 2. Action: Provo a scaricare gli esercizi
        System.out.println("Tentativo di connessione all'API...");
        List<Exercise> exercises = dao.findAll();

        // 3. Assert: Verifico che la lista non sia nulla e contenga dati
        assertNotNull(exercises, "La lista esercizi non dovrebbe mai essere null");
        assertFalse(exercises.isEmpty(), "L'API dovrebbe restituire almeno un esercizio");

        // Verifico che il primo esercizio abbia un nome
        assertNotNull(exercises.get(0).getName(), "L'esercizio deve avere un nome");

        System.out.println("Esercizi trovati: " + exercises.size());
        System.out.println("Test API: SUPERATO ✅");
    }
}