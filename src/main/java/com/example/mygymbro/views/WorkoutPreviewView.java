package com.example.mygymbro.views;

import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.PreviewController;

public interface WorkoutPreviewView extends View {
    void setListener(PreviewController controller);
    void showPlanDetails(WorkoutPlanBean plan);
}