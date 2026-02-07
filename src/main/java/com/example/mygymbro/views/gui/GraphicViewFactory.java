package com.example.mygymbro.views.gui;

import com.example.mygymbro.views.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class GraphicViewFactory implements ViewFactory {

    @Override
    public LoginView createLoginView() {
        try {
            // Assicurati che il percorso dell'FXML sia corretto!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mygymbro/view/view/schermataLogin.fxml"));
            Parent root = loader.load();
            GraphicLoginView view = loader.getController();
            view.setRoot(root);
            return view;
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return null;
        }
    }

     @Override
    public AthleteView createAthleteView() {
       try{
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mygymbro/view/view/mainpage.fxml"));
              Parent root = loader.load();
              GraphicAthleteView view = loader.getController();
              view.setRoot(root); //
           return view;
       }catch (IOException e){
           e.printStackTrace();
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
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public WorkoutPreviewView createWorkoutPreviewView() {
        return loadView("/com/example/mygymbro/view/view/WorkoutPreview.fxml");
    }

    @Override
    public LiveSessionView createLiveSessionView() {
        return loadView("/com/example/mygymbro/view/view/LiveSessionView.fxml"); // O LiveSession.fxml, controlla il nome file!
    }

    // Metodo helper (se lo stai usando, altrimenti usa il try-catch classico con FXMLLoader)
    private <T> T loadView(String path) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(path));
            javafx.scene.Parent root = loader.load();
            GraphicView view = loader.getController();
            view.setRoot(root);
            return (T) view;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}