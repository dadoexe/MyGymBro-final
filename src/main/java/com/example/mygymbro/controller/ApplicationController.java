package com.example.mygymbro.controller;

import com.example.mygymbro.bean.AthleteBean;
import com.example.mygymbro.bean.UserBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.views.*;
import com.example.mygymbro.views.cli.CliView;
import com.example.mygymbro.views.cli.CliViewFactory;
import com.example.mygymbro.views.gui.GraphicView;
import com.example.mygymbro.views.gui.GraphicViewFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class ApplicationController implements Controller {

    private static final Logger logger = Logger.getLogger(ApplicationController.class.getName());

    private static ApplicationController instance = null;

    private boolean isGraphicMode;
    private ViewFactory viewFactory;
    private Stage mainStage;
    private Controller currentController;

    private ApplicationController() {
        // Costruttore privato
    }

    public static synchronized ApplicationController getInstance() {
        if (instance == null) {
            instance = new ApplicationController();
        }
        return instance;
    }

    public void configure(boolean isGraphic, Stage stage) {
        this.isGraphicMode = isGraphic;
        this.mainStage = stage;

        if (isGraphic) {
            this.viewFactory = new GraphicViewFactory();
        } else {
            this.viewFactory = new CliViewFactory();
        }
    }

    public void start() {
        loadLogin();
    }

    // --- METODI DI NAVIGAZIONE ---

    public void loadLogin() {
        disposeCurrentController();

        LoginView view = viewFactory.createLoginView();
        LoginController controller = new LoginController(view);

        view.setListener(controller);
        this.currentController = controller;

        renderView(view);
    }

    public void loadHomeBasedOnRole() {
        UserBean user = SessionManager.getInstance().getCurrentUser();

        if (user == null) {
            loadLogin();
            return;
        }

        if ("TRAINER".equals(user.getRole())) {
            loadTrainerDashboard(null);
        } else {
            loadAthleteDashboard();
        }
    }

    public void loadTrainerDashboard() {
        loadTrainerDashboard(null);
    }

    public void loadTrainerDashboard(AthleteBean preSelectedAthlete) {
        disposeCurrentController();

        TrainerView view = viewFactory.createTrainerView();
        if (view == null) return;

        TrainerController controller = new TrainerController(view);
        view.setListener(controller);
        this.currentController = controller;

        controller.loadDashboardData();

        if (preSelectedAthlete != null) {
            view.setSelectedAthlete(preSelectedAthlete);
        }

        renderView(view);
    }

    public void loadAthleteDashboard() {
        disposeCurrentController();

        AthleteView view = viewFactory.createAthleteView();
        if (view == null) {
            logger.log(Level.SEVERE, "ERRORE CRITICO: La factory ha restituito una view NULL per loadAthleteDashboard!");
            return;
        }

        NavigationController controller = new NavigationController(view);
        view.setListener(controller);
        this.currentController = controller;

        controller.loadDashboardData();
        renderView(view);
    }

    public void loadWorkoutBuilder() {
        loadWorkoutBuilder(null, null);
    }

    public void loadWorkoutBuilderForClient(AthleteBean client) {
        loadWorkoutBuilder(null, client);
    }

    public void loadWorkoutBuilder(WorkoutPlanBean planToEdit, AthleteBean owner) {
        try {
            disposeCurrentController();

            WorkoutBuilderView view = viewFactory.createWorkoutBuilderView();
            if (view == null) return;

            PlanManagerController controller;
            if (planToEdit == null) {
                controller = new PlanManagerController(view);
            } else {
                controller = new PlanManagerController(view, planToEdit);
            }

            if (owner != null) {
                controller.setTargetAthlete(owner);
            }

            view.setListener(controller);
            this.currentController = controller;

            renderView(view);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Errore nel caricamento del WorkoutBuilder", e);
        }
    }

    public void loadWorkoutPreview(WorkoutPlanBean plan) {
        disposeCurrentController();

        WorkoutPreviewView view = viewFactory.createWorkoutPreviewView();
        if (view == null) return;

        PreviewController controller = new PreviewController(view, plan);
        view.setListener(controller);
        this.currentController = controller;

        renderView(view);
    }

    public void loadLiveSession(WorkoutPlanBean planToExecute) {
        disposeCurrentController();

        LiveSessionView view = viewFactory.createLiveSessionView();
        if (view == null) {
            logger.log(Level.SEVERE, "ERRORE: Factory LiveSessionView ha restituito null");
            return;
        }

        LiveSessionController controller = new LiveSessionController(view, planToExecute);
        view.setListener(controller);
        this.currentController = controller;

        renderView(view);
        controller.startSession();
    }

    public void logout() {
        SessionManager.getInstance().logout();
        loadLogin();
    }

    // --- METODI DI SUPPORTO ---

    private void disposeCurrentController() {
        if (currentController != null) {
            currentController.dispose();
            currentController = null;
        }
    }

    private void renderView(Object viewObject) {
        if (isGraphicMode) {
            // CORREZIONE JAVA MODERN (Pattern Matching): Risolve il warning su instanceof
            if (viewObject instanceof GraphicView graphicView) {
                Parent root = graphicView.getRoot();
                if (root != null && mainStage != null) {
                    Scene scene = new Scene(root);
                    mainStage.setScene(scene);
                    mainStage.show();
                }
            }
        } else {
            // CORREZIONE JAVA MODERN (Pattern Matching) anche per la CLI
            if (viewObject instanceof CliView cliView) {
                cliView.run();
            } else {
                logger.log(Level.WARNING, "Errore: La vista caricata non è una CLI valida: {0}", viewObject.getClass().getName());
            }
        }
    }

    @Override
    public void dispose() {
        disposeCurrentController();
        SessionManager.getInstance().logout();

        if (mainStage != null) {
            mainStage.close();
        }

        System.exit(0);
    }
}