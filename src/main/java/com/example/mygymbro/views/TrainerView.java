package com.example.mygymbro.views;

import com.example.mygymbro.bean.AthleteBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.TrainerController;

import java.util.List;

public interface TrainerView extends View{
    void setListener(TrainerController controller);
    void showAthletesList(List<AthleteBean> athletes);
    void showAthletePlans(List<WorkoutPlanBean> plans);
    void updateWelcomeMessage(String msg);
    AthleteBean getSelectedAthlete();
    WorkoutPlanBean getSelectedPlan();
    void setSelectedAthlete(AthleteBean athlete);

}
