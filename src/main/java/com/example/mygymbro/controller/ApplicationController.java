package com.example.mygymbro.controller;


import com.example.mygymbro.bean.UserBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.views.*;
import com.example.mygymbro.views.cli.CliViewFactory;
import com.example.mygymbro.views.gui.GraphicViewFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class ApplicationController implements Controller {//singleton

    //static variable reference of istance
    private static ApplicationController instance = null;
    boolean isGraphicMode; // O false se lanci da terminale

    private ViewFactory viewFactory; // LA NOSTRA NUOVA FACTORY!
    private Stage mainStage;         // Usato SOLO in modalità grafica

    //private contructor restricted to this class
    private ApplicationController() {
    }

    public static synchronized ApplicationController getInstance() {
        if (instance == null) {
            instance = new ApplicationController();
        }
        return instance;

    }

    // --- 2. GESTIONE DEL CONTROLLER ATTUALE ---
    private Controller currentController; // L'interfaccia generica che abbiamo creato

    public void configure(boolean isGraphic, Stage stage) {
        this.isGraphicMode = isGraphic;
        this.mainStage = stage; // Sarà null se siamo in CLI, ma va bene!

        // Inizializza la Factory corretta
        if (isGraphic) {
            this.viewFactory = new GraphicViewFactory();
        } else {
            this.viewFactory = new CliViewFactory();
        }
    }

    // Avvio dell'applicazione
    public void start() {
        loadLogin(); // Carica la prima schermata
    }

    //METODI DI NAVIGAZIONE
    public void loadLogin() {
        if (currentController != null) currentController.dispose();

        // 1. CHIEDIAMO ALLA FACTORY (Polimorfismo)
        // Se siamo in GUI, ci dà GraphicLoginView. Se siamo in CLI, ci dà CliLoginView.
        LoginView view = viewFactory.createLoginView();

        // 2. SETUP CONTROLLER (Identico per entrambi)
        LoginController controller = new LoginController(view);
        view.setListener(controller);
        this.currentController = controller;

        // 3. MOSTRARE LA VISTA (Qui dobbiamo gestire la differenza di "contenitore")
        renderView(view);
    }

    private void renderView(Object viewObject) {
        if (isGraphicMode) {
            // --- LOGICA JAVAFX (Resta uguale) ---
            if (viewObject instanceof com.example.mygymbro.views.gui.GraphicView) {
                Parent root = ((com.example.mygymbro.views.gui.GraphicView) viewObject).getRoot();
                if (root != null) {
                    Scene scene = new Scene(root);
                    mainStage.setScene(scene);
                    mainStage.show();
                }
            }
        } else {
            // --- LOGICA CLI (CORRETTA) ---
            // Controlliamo se è una vista CLI valida
            if (viewObject instanceof com.example.mygymbro.views.cli.CliView) {
                // Facciamo il cast a CliView e chiamiamo run()
                ((com.example.mygymbro.views.cli.CliView) viewObject).run();
            } else {
                System.err.println("Errore: La vista caricata non è una CLI valida.");
            }
        }
    }

    public void loadHomeBasedOnRole() {
        UserBean user = SessionManager.getInstance().getCurrentUser();

        if (user == null) {
            loadLogin();
            return;
        }

        if ("TRAINER".equals(user.getRole())) {
            loadTrainerDashboard(); // <--- Crea questo metodo!
        } else {
            loadAthleteDashboard(); // <--- Questo è il vecchio loadHome() rinominato
        }
    }

    public void loadTrainerDashboard(com.example.mygymbro.bean.AthleteBean preSelectedAthlete) {
        if (currentController != null) currentController.dispose();

        TrainerView view = viewFactory.createTrainerView();
        if (view == null) return;

        TrainerController controller = new TrainerController(view);
        view.setListener(controller);
        this.currentController = controller;

        controller.loadDashboardData();

        // SE AVEVAMO UN CLIENTE SELEZIONATO, RIPRISTINALO!
        if (preSelectedAthlete != null) {
            view.setSelectedAthlete(preSelectedAthlete);
        }

        renderView(view);
    }

    // Overload per compatibilità (chiama quello sopra con null)
    public void loadTrainerDashboard() {
        loadTrainerDashboard(null);
    }

    public void loadAthleteDashboard() {
        // 1. Pulizia del controller precedente
        if (currentController != null) {
            currentController.dispose();
        }

        // 2. CREAZIONE VISTA TRAMITE FACTORY (Il pezzo che mancava!)
        // La factory deciderà se creare GraphicAthleteView o CliAthleteView
        AthleteView view = viewFactory.createAthleteView();

        // Controllo di sicurezza
        if (view == null) {
            System.err.println("ERRORE CRITICO: La factory ha restituito una view NULL per loadHome!");
            return;
        }


        // 3. Setup del Controller
        NavigationController controller = new NavigationController(view);
        view.setListener(controller);
        this.currentController = controller;

        // 4. Caricamento dati iniziali (Dashboard)
        controller.loadDashboardData();

        // 5. Mostra a video (Render)
        renderView(view);
    }


    // VERSIONE AGGIORNATA: Accetta anche l'atleta proprietario (owner)
    public void loadWorkoutBuilder(WorkoutPlanBean planToEdit, com.example.mygymbro.bean.AthleteBean owner) {
        try {
            // 1. Pulizia
            if (currentController != null) {
                currentController.dispose();
            }

            // 2. CREAZIONE VISTA
            WorkoutBuilderView view = viewFactory.createWorkoutBuilderView();
            if (view == null) return;

            // 3. Setup Controller
            PlanManagerController controller;
            if (planToEdit == null) {
                controller = new PlanManagerController(view);
            } else {
                controller = new PlanManagerController(view, planToEdit);
            }

            // --- FIX CRUCIALE: Se abbiamo un proprietario (es. Mario), lo passiamo al controller ---
            if (owner != null) {
                controller.setTargetAthlete(owner);
            }
            // --------------------------------------------------------------------------------------

            view.setListener(controller);
            this.currentController = controller;

            // 4. Render
            renderView(view);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento del WorkoutBuilder: " + e.getMessage());
        }
    }

    // Overload per mantenere compatibilità con chi non passa l'atleta (es. tasto "Nuova Scheda" generico)
    public void loadWorkoutBuilder() {
        loadWorkoutBuilder(null, null);
    }

    public void loadWorkoutBuilderForClient(com.example.mygymbro.bean.AthleteBean client) {
        try {
            if (currentController != null) currentController.dispose();

            // 1. Crea la View
            WorkoutBuilderView view = viewFactory.createWorkoutBuilderView();
            if (view == null) return;

            // 2. Crea il Controller in modalità "NUOVA SCHEDA"
            PlanManagerController controller = new PlanManagerController(view);

            // 3. SETTA IL TARGET: Diciamo al controller che il proprietario sarà il cliente!
            controller.setTargetAthlete(client);

            view.setListener(controller);
            this.currentController = controller;
            renderView(view);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore loadWorkoutBuilderForClient: " + e.getMessage());
        }
    }
    // ... altri metodi ...

    public void loadWorkoutPreview(WorkoutPlanBean plan) {
        if (currentController != null) currentController.dispose();

        // 1. Crea la View (Dovrai aggiungere createWorkoutPreviewView alla Factory!)
        WorkoutPreviewView view = viewFactory.createWorkoutPreviewView();

        if (view == null) return;

        // 2. Crea Controller
        PreviewController controller = new PreviewController(view, plan);
        view.setListener(controller);
        this.currentController = controller;

        renderView(view);
    }
    public void loadLiveSession(WorkoutPlanBean planToExecute) {
        if (currentController != null) currentController.dispose();

        // 1. Crea View (GUI o CLI tramite factory)
        LiveSessionView view = viewFactory.createLiveSessionView();

        if (view == null) {
            System.err.println("ERRORE: Factory LiveSessionView ha restituito null");
            return;
        }

        // 2. Crea Controller
        LiveSessionController controller = new LiveSessionController(view, planToExecute);
        view.setListener(controller);
        this.currentController = controller;

        // 3. Render
        renderView(view);
        controller.startSession();
    }

    public void logout() {
        // Pulisco la sessione
        SessionManager.getInstance().logout();
        // Torno al login
        loadLogin();
    }

    @Override
    public void dispose() {
        // 1. Chiudo il controller della schermata attuale (se esiste)
        // Questo è fondamentale se quel controller ha connessioni aperte o thread attivi!
        if (currentController != null) {
            currentController.dispose();
        }

        // 2. Pulisco la sessione (Logout forzato)
        SessionManager.getInstance().logout();

        // 3. Chiudo la finestra principale (se è ancora aperta)
        if (mainStage != null) {
            mainStage.close();
        }

        System.exit(0);
    }
}