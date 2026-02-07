package com.example.mygymbro;

import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class WorkoutPlanTest {

    @Test
    void testCreateWorkoutPlan() {
        // 1. Setup: Creo una nuova scheda
        WorkoutPlanBean plan = new WorkoutPlanBean();
        plan.setName("Scheda Massa");
        plan.setComment("Obiettivo Ipertrofia");
        plan.setExerciseList(new ArrayList<>());

        // 2. Action: Aggiungo un esercizio
        WorkoutExerciseBean ex = new WorkoutExerciseBean();
        ex.setExerciseName("Panca Piana");
        ex.setSets(4);
        ex.setReps(10);

        plan.getExerciseList().add(ex);

        // 3. Assert: Verifico che i dati siano corretti
        assertEquals("Scheda Massa", plan.getName(), "Il nome della scheda dovrebbe corrispondere");
        assertEquals(1, plan.getExerciseList().size(), "La scheda dovrebbe contenere 1 esercizio");
        assertEquals("Panca Piana", plan.getExerciseList().get(0).getExerciseName(), "Il nome dell'esercizio deve corrispondere");

        System.out.println("Test Creazione Scheda: SUPERATO ✅");
    }
}