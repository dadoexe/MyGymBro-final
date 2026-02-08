package com.example.mygymbro.views.gui;

import com.example.mygymbro.views.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphicViewFactory implements ViewFactory {

    private static final Logger LOGGER = Logger.getLogger(GraphicViewFactory.class.getName());

    @Override
    public LoginView createLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mygymbro/view/view/schermataLogin.fxml"));
            Parent root = loader.load();
            GraphicLoginView view = loader.getController();
            view.setRoot(root);
            return view;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento della vista Login", e);
            return null;
        }
    }

    @Override
    public WorkoutBuilderView createWorkoutBuilderView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mygymbro/view/view/workout_builder.fxml"));
            Parent root = loader.load();
            GraphicWorkoutBuilderView view = loader.getController();
            view.setRoot(root);
            return view;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento della vista WorkoutBuilder", e);
            return null;
        }
    }

    @Override
    public AthleteView createAthleteView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mygymbro/view/view/mainpage.fxml"));
            Parent root = loader.load();
            GraphicAthleteView view = loader.getController();
            view.setRoot(root);
            return view;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento della vista Athlete", e);
            return null;
        }
    }

    @Override
    public TrainerView createTrainerView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mygymbro/view/view/trainer_view.fxml"));
            Parent root = loader.load();
            GraphicTrainerView view = loader.getController();
            view.setRoot(root);
            return view;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento della vista Trainer", e);
            return null;
        }
    }

    @Override
    public WorkoutPreviewView createWorkoutPreviewView() {
        return loadView("/com/example/mygymbro/view/view/WorkoutPreview.fxml", "WorkoutPreview");
    }

    @Override
    public LiveSessionView createLiveSessionView() {
        return loadView("/com/example/mygymbro/view/view/LiveSessionView.fxml", "LiveSession");
    }

    // Metodo helper generico per caricare le view
    private <T> T loadView(String path, String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            GraphicView view = loader.getController();
            view.setRoot(root);
            return (T) view;
        } catch (IOException e) {
            String message = String.format("Errore durante il caricamento della vista %s", viewName);
            LOGGER.log(Level.SEVERE, message, e);
            return null;
        }
    }
}