package com.example.mygymbro.views;

import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.NavigationController;
import java.util.List;

public interface AthleteView extends View {
    void setListener(NavigationController controller);

    // Uniamo tutto in un unico metodo chiaro
    void updateWorkoutList(List<WorkoutPlanBean> workoutPlans);

    void updateWelcomeMessage(String msg);

}