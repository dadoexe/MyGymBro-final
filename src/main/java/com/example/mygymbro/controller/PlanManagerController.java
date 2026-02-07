package com.example.mygymbro.controller;

import com.example.mygymbro.bean.*;
import com.example.mygymbro.dao.DAOFactory;
import com.example.mygymbro.dao.ExerciseDAO;
import com.example.mygymbro.dao.WorkoutPlanDAO;
import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.*;
import com.example.mygymbro.views.WorkoutBuilderView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlanManagerController implements Controller {

    private static final Logger LOGGER = Logger.getLogger(PlanManagerController.class.getName());
    private static final String TRAINER_ROLE = "TRAINER";
    private static final String DEFAULT_MUSCLE_GROUP = "UNKNOWN";
    private static final int SECONDS_PER_REP = 4;
    private static final int TRANSITION_TIME = 60;

    private WorkoutBuilderView view;
    private WorkoutPlanDAO workoutPlanDAO;
    private ExerciseDAO exerciseDAO;
    private WorkoutPlanBean currentPlan;
    private AthleteBean targetAthlete; // Fondamentale per il Trainer

    // --- COSTRUTTORE 1: CREAZIONE NUOVO PIANO ---
    public PlanManagerController(WorkoutBuilderView view) {
        this.view = view;
        this.workoutPlanDAO = DAOFactory.getWorkoutPlanDAO();
        this.exerciseDAO = DAOFactory.getExerciseDAO();
        this.currentPlan = new WorkoutPlanBean();
        this.currentPlan.setExerciseList(new ArrayList<>());
        loadAvailableExercises();
    }

    // --- COSTRUTTORE 2: MODIFICA PIANO ESISTENTE ---
    public PlanManagerController(WorkoutBuilderView view, WorkoutPlanBean planToEdit) {
        this.view = view;
        this.workoutPlanDAO = DAOFactory.getWorkoutPlanDAO();
        this.exerciseDAO = DAOFactory.getExerciseDAO();
        this.currentPlan = planToEdit;
        populateViewWithPlanData();
        loadAvailableExercises();
    }

    public void setTargetAthlete(AthleteBean athlete) {
        this.targetAthlete = athlete;
    }

    // --- LOGICA DI SALVATAGGIO (UNICA E CORRETTA) ---
    public void handleSavePlan() {
        try {
            WorkoutPlanBean formData = view.getWorkoutPlanBean();

            updateCurrentPlan(formData);

            if (!validatePlan()) {
                return;
            }

            savePlanToDatabase();

            view.showSuccess("Scheda salvata correttamente!");
            navigateAfterSave();

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio della scheda", e);
            view.showError("Errore durante il salvataggio: " + e.getMessage());
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Stato non valido durante il salvataggio", e);
            view.showError("Errore: " + e.getMessage());
        }
    }

    private void updateCurrentPlan(WorkoutPlanBean formData) {
        currentPlan.setName(formData.getName());
        currentPlan.setComment(formData.getComment());
        currentPlan.setExerciseList(formData.getExerciseList());
    }

    private boolean validatePlan() {
        if (currentPlan.getName() == null || currentPlan.getName().trim().isEmpty()) {
            view.showError("Devi dare un nome alla scheda!");
            return false;
        }
        if (currentPlan.getExerciseList() == null || currentPlan.getExerciseList().isEmpty()) {
            view.showError("La scheda deve avere almeno un esercizio.");
            return false;
        }
        return true;
    }

    private void savePlanToDatabase() throws DAOException {
        WorkoutPlan planModel = toModelWorkoutPlan(currentPlan);

        if (currentPlan.getId() > 0) {
            workoutPlanDAO.update(planModel);
        } else {
            workoutPlanDAO.save(planModel);
        }
    }

    private void navigateAfterSave() {
        UserBean currentUser = SessionManager.getInstance().getCurrentUser();

        if (isTrainerWorkingOnClient(currentUser)) {
            ApplicationController.getInstance().loadTrainerDashboard(this.targetAthlete);
        } else {
            ApplicationController.getInstance().loadHomeBasedOnRole();
        }
    }

    private boolean isTrainerWorkingOnClient(UserBean user) {
        return TRAINER_ROLE.equals(user.getRole()) && this.targetAthlete != null;
    }

    // --- ANNULLA ---
    public void handleCancel() {
        UserBean currentUser = SessionManager.getInstance().getCurrentUser();

        if (isTrainerWorkingOnClient(currentUser)) {
            ApplicationController.getInstance().loadTrainerDashboard(this.targetAthlete);
        } else {
            ApplicationController.getInstance().loadHomeBasedOnRole();
        }
    }

    // Metodo legacy per la CLI (se serve), lo mappiamo su handleSavePlan
    public void submit() {
        handleSavePlan();
    }

    // --- METODI DI SUPPORTO (Exercise, Time, ecc.) ---

    public void addExerciseToPlan(WorkoutExerciseBean newExercise) {
        if (newExercise == null) return;
        if (currentPlan.getExerciseList() == null) {
            currentPlan.setExerciseList(new ArrayList<>());
        }

        currentPlan.getExerciseList().add(newExercise);
        view.updateExerciseTable(currentPlan.getExerciseList());
        calculateDuration();
    }

    public void removeExerciseFromPlan(WorkoutExerciseBean exerciseToRemove) {
        if (currentPlan != null && currentPlan.getExerciseList() != null) {
            currentPlan.getExerciseList().remove(exerciseToRemove);
            view.updateExerciseTable(currentPlan.getExerciseList());
            calculateDuration();
        }
    }

    private void calculateDuration() {
        int totalSeconds = 0;
        if (currentPlan != null && currentPlan.getExerciseList() != null) {
            for (WorkoutExerciseBean ex : currentPlan.getExerciseList()) {
                totalSeconds += calculateExerciseDuration(ex);
            }
        }
        view.updateTotalTime("Durata stimata: ~" + (totalSeconds / 60) + " min");
    }

    private int calculateExerciseDuration(WorkoutExerciseBean exercise) {
        int activeTime = exercise.getSets() * exercise.getReps() * SECONDS_PER_REP;
        int restTime = exercise.getSets() * exercise.getRestTime();
        return activeTime + restTime + TRANSITION_TIME;
    }

    private void loadAvailableExercises() {
        try {
            List<Exercise> exercises = exerciseDAO.findAll();
            List<ExerciseBean> beans = exercises.stream()
                    .map(this::toExerciseBean)
                    .toList();
            view.populateExerciseMenu(beans);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Impossibile caricare gli esercizi disponibili", e);
            view.populateExerciseMenu(new ArrayList<>());
        }
    }

    public List<ExerciseBean> searchExercisesOnApi(String keyword) {
        List<Exercise> results = exerciseDAO.search(keyword);
        return results.stream()
                .map(this::toExerciseBean)
                .toList();
    }

    private void populateViewWithPlanData() {
        if (currentPlan == null) return;
        view.setPlanName(currentPlan.getName());
        view.setPlanComment(currentPlan.getComment());
        view.updateExerciseTable(currentPlan.getExerciseList());
        calculateDuration();
    }

    // --- MAPPERS (Bean <-> Model) ---

    private ExerciseBean toExerciseBean(Exercise e) {
        ExerciseBean bean = new ExerciseBean();
        bean.setId(String.valueOf(e.getId()));
        bean.setName(e.getName());
        bean.setDescription(e.getDescription());
        bean.setMuscleGroup(e.getMuscleGroup() != null ? e.getMuscleGroup().name() : DEFAULT_MUSCLE_GROUP);
        return bean;
    }

    private WorkoutPlan toModelWorkoutPlan(WorkoutPlanBean bean) {
        if (bean == null) {
            return null;
        }

        int ownerId = determineOwnerId();

        Athlete athlete = new Athlete();
        athlete.setId(ownerId);

        WorkoutPlan plan = new WorkoutPlan(
                bean.getId(),
                bean.getName(),
                bean.getComment(),
                new Date(),
                athlete
        );

        if (bean.getExerciseList() != null) {
            for (WorkoutExerciseBean web : bean.getExerciseList()) {
                plan.addExercise(toModelWorkoutExercise(web));
            }
        }
        return plan;
    }

    private int determineOwnerId() {
        if (this.targetAthlete != null) {
            return this.targetAthlete.getId();
        }

        UserBean currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nessun utente loggato");
        }
        return currentUser.getId();
    }

    private WorkoutExercise toModelWorkoutExercise(WorkoutExerciseBean bean) {
        MuscleGroup muscleGroup = parseMuscleGroup(bean.getMuscleGroup());
        Exercise definition = new Exercise(0, bean.getExerciseName(), "Custom", muscleGroup);
        return new WorkoutExercise(definition, bean.getSets(), bean.getReps(), bean.getRestTime());
    }

    private MuscleGroup parseMuscleGroup(String muscleGroupStr) {
        if (muscleGroupStr == null) {
            return MuscleGroup.CHEST;
        }

        try {
            return MuscleGroup.valueOf(muscleGroupStr);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Gruppo muscolare non valido: " + muscleGroupStr, e);
            return MuscleGroup.CHEST;
        }
    }

    @Override
    public void dispose() {
        this.workoutPlanDAO = null;
        this.exerciseDAO = null;
    }
}
