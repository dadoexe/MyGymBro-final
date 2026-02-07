package com.example.mygymbro.controller;

import com.example.mygymbro.bean.UserBean;
import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.dao.DAOFactory;
import com.example.mygymbro.dao.WorkoutPlanDAO;
import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.WorkoutPlan;
import com.example.mygymbro.views.AthleteView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NavigationController implements Controller {

    private static final Logger LOGGER = Logger.getLogger(NavigationController.class.getName());
    private static final String DEFAULT_EXERCISE_NAME = "Esercizio";
    private static final String DEFAULT_MUSCLE_GROUP = "Misto";

    private AthleteView view;
    private WorkoutPlanDAO workoutPlanDAO;
    // currentUser lo recuperiamo dinamicamente, non serve salvarlo come attributo fisso
    // per evitare che rimanga vecchio se l'utente cambia (caso raro, ma safer).

    public NavigationController(AthleteView view) {
        this.view = view;
        this.workoutPlanDAO = DAOFactory.getWorkoutPlanDAO();
    }

    public void loadDashboardData() {
        UserBean currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            logout(); // Se la sessione è scaduta, via al login
            return;
        }

        view.updateWelcomeMessage(currentUser.getUsername());

        try {
            // Preparazione dati per il DAO
            Athlete currentAthleteModel = new Athlete();
            currentAthleteModel.setId(currentUser.getId());
            currentAthleteModel.setUsername(currentUser.getUsername());

            // Chiamata al DAO
            List<WorkoutPlan> plans = workoutPlanDAO.findByAthlete(currentAthleteModel);

            // Conversione
            List<WorkoutPlanBean> planBeans = convertModelsToBeans(plans);

            // Aggiornamento View
            view.updateWorkoutList(planBeans);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento delle schede", e);
            view.updateWorkoutList(new ArrayList<>());
            view.showError("Impossibile caricare le schede.");
        }
    }

    public void openPlanPreview(WorkoutPlanBean plan) {
        ApplicationController.getInstance().loadWorkoutPreview(plan);
    }

    public void startLiveSession(WorkoutPlanBean plan) {
        if (plan == null) return;
        // Deleghiamo all'ApplicationController il cambio di scena (o di vista CLI)
        ApplicationController.getInstance().loadLiveSession(plan);
    }

    // --- METODI DI AZIONE (Standardizzati per CLI e GUI) ---
    public void handleCreateNewPlan() {
        ApplicationController.getInstance().loadWorkoutBuilder();
    }

    // 1. Modifica Scheda
    public void modifyPlan(WorkoutPlanBean plan) {
        // L'atleta modifica se stesso, non serve specificare un owner esterno (sarà null)
        ApplicationController.getInstance().loadWorkoutBuilder(plan, null);
    }

    // 2. Elimina Scheda
    // Rinominato da 'handleDeletePlan' a 'deletePlan' per coerenza con la View CLI
    public void deletePlan(WorkoutPlanBean planBean) {
        try {
            workoutPlanDAO.delete(planBean.getId());

            view.showSuccess("Scheda '" + planBean.getName() + "' eliminata.");

            loadDashboardData();

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione della scheda", e);
            view.showError("Errore cancellazione: " + e.getMessage());
        }
    }

    // --- NAVIGAZIONE ---

    public void loadWorkoutBuilder() {
        // Nuova scheda vuota
        ApplicationController.getInstance().loadWorkoutBuilder();
    }

    public void logout() {
        // Unica fonte di verità per il logout
        ApplicationController.getInstance().logout();
    }

    @Override
    public void dispose() {
        this.workoutPlanDAO = null;
    }

    // --- HELPER DI CONVERSIONE ---
    private List<WorkoutPlanBean> convertModelsToBeans(List<WorkoutPlan> models) {
        List<WorkoutPlanBean> beans = new ArrayList<>();
        if (models == null) return beans;

        for (WorkoutPlan plan : models) {
            WorkoutPlanBean bean = convertPlanToBean(plan);
            beans.add(bean);
        }
        return beans;
    }

    private WorkoutPlanBean convertPlanToBean(WorkoutPlan plan) {
        WorkoutPlanBean bean = new WorkoutPlanBean();
        bean.setId(plan.getId());
        bean.setName(plan.getName());
        bean.setComment(plan.getComment());
        bean.setCreationDate(plan.getCreationDate());

        List<WorkoutExerciseBean> exerciseBeans = convertExercisesToBeans(plan.getExercises());
        bean.setExerciseList(exerciseBeans);

        return bean;
    }

    private List<WorkoutExerciseBean> convertExercisesToBeans(List<com.example.mygymbro.model.WorkoutExercise> exercises) {
        List<WorkoutExerciseBean> exerciseBeans = new ArrayList<>();

        if (exercises == null) {
            return exerciseBeans;
        }

        for (com.example.mygymbro.model.WorkoutExercise modelEx : exercises) {
            WorkoutExerciseBean exBean = convertExerciseToBean(modelEx);
            exerciseBeans.add(exBean);
        }

        return exerciseBeans;
    }

    private WorkoutExerciseBean convertExerciseToBean(com.example.mygymbro.model.WorkoutExercise modelEx) {
        WorkoutExerciseBean exBean = new WorkoutExerciseBean();

        exBean.setExerciseName(extractExerciseName(modelEx));
        exBean.setMuscleGroup(extractMuscleGroup(modelEx));
        exBean.setSets(modelEx.getSets());
        exBean.setReps(modelEx.getReps());
        exBean.setRestTime(modelEx.getRestTime());

        return exBean;
    }

    private String extractExerciseName(com.example.mygymbro.model.WorkoutExercise modelEx) {
        if (modelEx.getExercise() != null) {
            return modelEx.getExercise().getName();
        }

        if (modelEx.getExerciseDefinition() != null) {
            return modelEx.getExerciseDefinition().getName();
        }

        return DEFAULT_EXERCISE_NAME;
    }

    private String extractMuscleGroup(com.example.mygymbro.model.WorkoutExercise modelEx) {
        if (modelEx.getExercise() != null && modelEx.getExercise().getMuscleGroup() != null) {
            return modelEx.getExercise().getMuscleGroup().name();
        }

        if (modelEx.getExerciseDefinition() != null && modelEx.getExerciseDefinition().getMuscleGroup() != null) {
            return modelEx.getExerciseDefinition().getMuscleGroup().name();
        }

        return DEFAULT_MUSCLE_GROUP;
    }
}