package com.example.mygymbro.views;

import com.example.mygymbro.bean.ExerciseBean;
import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.PlanManagerController;
import java.util.List;

public interface WorkoutBuilderView extends View {

    void setListener(PlanManagerController controller);

    // Gestione Nomi/Commenti
    String getPlanName();
    void setPlanName(String name);
    String getComment();
    void setPlanComment(String comment);

    // Gestione Esercizi
    void populateExerciseMenu(List<ExerciseBean> exercises);
    void updateExerciseTable(List<WorkoutExerciseBean> exercises);
    List<WorkoutExerciseBean> getAddedExercises();
    void updateExerciseList(List<WorkoutExerciseBean> exercises);
    // Gestione Tempo
    void updateTotalTime(String timeMessage);
    WorkoutPlanBean getWorkoutPlanBean();
}

