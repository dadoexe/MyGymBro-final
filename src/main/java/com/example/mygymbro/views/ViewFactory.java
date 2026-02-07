package com.example.mygymbro.views;

public interface ViewFactory {
    // Definiamo i metodi di creazione per ogni schermata dell'app
    LoginView createLoginView();
    WorkoutBuilderView createWorkoutBuilderView();
    AthleteView createAthleteView(); // Scommenta quando avrai la AthleteView
    TrainerView createTrainerView();
    WorkoutPreviewView createWorkoutPreviewView();
    LiveSessionView createLiveSessionView();
}