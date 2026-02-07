package com.example.mygymbro.controller;

import com.example.mygymbro.bean.AthleteBean;
import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.dao.DAOFactory;
import com.example.mygymbro.dao.UserDAO;
import com.example.mygymbro.dao.WorkoutPlanDAO;
import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.WorkoutExercise;
import com.example.mygymbro.model.WorkoutPlan;
import com.example.mygymbro.views.TrainerView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainerController implements Controller {

    private static final Logger LOGGER = Logger.getLogger(TrainerController.class.getName());
    private static final String DEFAULT_EXERCISE_NAME = "Sconosciuto";

    private TrainerView view;
    private UserDAO userDAO;
    private WorkoutPlanDAO workoutPlanDAO;

    public TrainerController(TrainerView view) {
        this.view = view;
        this.userDAO = DAOFactory.getUserDAO();
        this.workoutPlanDAO = DAOFactory.getWorkoutPlanDAO();
    }

    public void loadDashboardData() {
        // 1. Aggiorna Messaggio
        String trainerName = SessionManager.getInstance().getCurrentUser().getUsername();
        view.updateWelcomeMessage(trainerName);

        // 2. Carica Lista Clienti
        loadAllAthletes();
    }

    private void loadAllAthletes() {
        try {
            // MOCK DATI (O usa userDAO.findAllAthletes() se implementato)
            List<AthleteBean> dummyAthletes = createMockAthletes();
            view.showAthletesList(dummyAthletes);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento degli atleti", e);
        }
    }

    private List<AthleteBean> createMockAthletes() {
        List<AthleteBean> athletes = new ArrayList<>();

        AthleteBean a1 = new AthleteBean();
        a1.setUsername("mario");
        a1.setNome("Mario");
        a1.setCognome("Rossi");
        a1.setId(1);

        AthleteBean a2 = new AthleteBean();
        a2.setUsername("luigi");
        a2.setNome("Luigi");
        a2.setCognome("Verdi");
        a2.setId(2);

        athletes.add(a1);
        athletes.add(a2);

        return athletes;
    }

    public void loadPlansForAthlete(AthleteBean athleteBean) {
        try {
            Athlete model = new Athlete();
            model.setId(athleteBean.getId());

            List<WorkoutPlan> plans = workoutPlanDAO.findByAthlete(model);
            List<WorkoutPlanBean> beans = convertModelsToBeans(plans);

            view.showAthletePlans(beans);

        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il caricamento delle schede", e);
            view.showError("Errore caricamento schede.");
        }
    }

    public void createNewPlan() {
        AthleteBean selectedAthlete = view.getSelectedAthlete();
        if (selectedAthlete != null) {
            LOGGER.log(Level.INFO, "Creazione scheda per: {0}", selectedAthlete.getUsername());
            ApplicationController.getInstance().loadWorkoutBuilderForClient(selectedAthlete);
        } else {
            view.showError("Seleziona un cliente dalla lista!");
        }
    }

    public void modifySelectedPlan() {
        WorkoutPlanBean plan = view.getSelectedPlan();
        AthleteBean currentClient = view.getSelectedAthlete();

        if (plan != null) {
            LOGGER.log(Level.INFO, "Modifica scheda: {0}", plan.getName());
            ApplicationController.getInstance().loadWorkoutBuilder(plan, currentClient);
        } else {
            view.showError("Nessuna scheda selezionata!");
        }
    }

    public void logout() {
        ApplicationController.getInstance().logout();
    }

    @Override
    public void dispose() {
        // Pulizia risorse se necessarie
    }

    // --- HELPER DI CONVERSIONE COMPLETO ---
    private List<WorkoutPlanBean> convertModelsToBeans(List<WorkoutPlan> models) {
        List<WorkoutPlanBean> beans = new ArrayList<>();
        if (models == null) {
            return beans;
        }

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

        List<WorkoutExerciseBean> exerciseBeans = convertExercisesToBeans(plan.getExercises());
        bean.setExerciseList(exerciseBeans);

        return bean;
    }

    private List<WorkoutExerciseBean> convertExercisesToBeans(List<WorkoutExercise> exercises) {
        List<WorkoutExerciseBean> exerciseBeans = new ArrayList<>();

        if (exercises == null) {
            return exerciseBeans;
        }

        for (WorkoutExercise modelEx : exercises) {
            WorkoutExerciseBean exBean = convertExerciseToBean(modelEx);
            exerciseBeans.add(exBean);
        }

        return exerciseBeans;
    }

    private WorkoutExerciseBean convertExerciseToBean(WorkoutExercise modelEx) {
        WorkoutExerciseBean exBean = new WorkoutExerciseBean();

        exBean.setExerciseName(extractExerciseName(modelEx));
        exBean.setMuscleGroup(extractMuscleGroup(modelEx));
        exBean.setSets(modelEx.getSets());
        exBean.setReps(modelEx.getReps());
        exBean.setRestTime(modelEx.getRestTime());

        return exBean;
    }

    private String extractExerciseName(WorkoutExercise modelEx) {
        if (modelEx.getExercise() != null) {
            return modelEx.getExercise().getName();
        }
        return DEFAULT_EXERCISE_NAME;
    }

    private String extractMuscleGroup(WorkoutExercise modelEx) {
        if (modelEx.getExercise() != null && modelEx.getExercise().getMuscleGroup() != null) {
            return modelEx.getExercise().getMuscleGroup().name();
        }
        return null;
    }
}
