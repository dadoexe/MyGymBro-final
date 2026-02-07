package com.example.mygymbro.controller;

import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.views.WorkoutPreviewView;

public class PreviewController implements Controller {

    private WorkoutPreviewView view;
    private WorkoutPlanBean currentPlan;

    public PreviewController(WorkoutPreviewView view, WorkoutPlanBean plan) {
        this.view = view;
        this.currentPlan = plan;
        // Appena parte, popola la vista
        view.showPlanDetails(plan);
    }

    public void startSession() {
        // Qui avviene la magia: passiamo dalla Preview alla Live Session
        ApplicationController.getInstance().loadLiveSession(currentPlan);
    }

    public void back() {
        ApplicationController.getInstance().loadHomeBasedOnRole();
    }

    @Override
    public void dispose() {
        // cleanup se serve
    }
}