package com.example.mygymbro;

import com.example.mygymbro.bean.WorkoutExerciseBean;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExerciseValidationTest {

    @Test
    void testValidExerciseData() {
        // CASO 1: Dati validi normali
        WorkoutExerciseBean exercise = new WorkoutExerciseBean();
        exercise.setExerciseName("Panca Piana");
        exercise.setSets(3);
        exercise.setReps(10);
        exercise.setRestTime(60);

        assertTrue(exercise.getSets() > 0, "Serie devono essere positive");
        assertTrue(exercise.getReps() > 0, "Ripetizioni devono essere positive");
        assertTrue(exercise.getRestTime() >= 0, "Recupero non può essere negativo");
        assertNotNull(exercise.getExerciseName(), "Nome obbligatorio");

        System.out.println("✅ Test dati validi: PASS");
    }

    @Test
    void testMinimumValidValues() {
        // CASO 2: Valori minimi accettabili
        WorkoutExerciseBean exercise = new WorkoutExerciseBean();
        exercise.setExerciseName("Plank");
        exercise.setSets(1);          // Minimo 1 serie
        exercise.setReps(1);          // Minimo 1 ripetizione
        exercise.setRestTime(0);      // Zero recupero è valido

        assertTrue(exercise.getSets() >= 1, "Deve avere almeno 1 serie");
        assertTrue(exercise.getReps() >= 1, "Deve avere almeno 1 ripetizione");
        assertTrue(exercise.getRestTime() >= 0, "Recupero >= 0");

        System.out.println("✅ Test valori minimi: PASS");
    }

    @Test
    void testRealisticWorkoutValues() {
        // CASO 3: Valori realistici per un allenamento
        WorkoutExerciseBean exercise = new WorkoutExerciseBean();
        exercise.setExerciseName("Squat");
        exercise.setSets(5);
        exercise.setReps(5);
        exercise.setRestTime(180); // 3 minuti per esercizio pesante

        // Verifica range realistico
        assertTrue(exercise.getSets() >= 1 && exercise.getSets() <= 10,
                "Serie dovrebbero essere tra 1 e 10");
        assertTrue(exercise.getReps() >= 1 && exercise.getReps() <= 50,
                "Ripetizioni tra 1 e 50");
        assertTrue(exercise.getRestTime() >= 0 && exercise.getRestTime() <= 300,
                "Recupero massimo 5 minuti");

        System.out.println("✅ Test valori realistici: PASS");
    }
}