package com.example.mygymbro.controller;

import com.example.mygymbro.bean.AthleteBean;
import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.dao.DAOFactory;
import com.example.mygymbro.dao.UserDAO;
import com.example.mygymbro.dao.WorkoutPlanDAO;
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.WorkoutExercise;
import com.example.mygymbro.model.WorkoutPlan;
import com.example.mygymbro.views.TrainerView;

import java.util.ArrayList;
import java.util.List;

public class TrainerController implements Controller {

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
            List<AthleteBean> dummyAthletes = new ArrayList<>();
            AthleteBean a1 = new AthleteBean(); a1.setUsername("mario"); a1.setNome("Mario"); a1.setCognome("Rossi"); a1.setId(1);
            AthleteBean a2 = new AthleteBean(); a2.setUsername("luigi"); a2.setNome("Luigi"); a2.setCognome("Verdi"); a2.setId(2);
            dummyAthletes.add(a1);
            dummyAthletes.add(a2);

            view.showAthletesList(dummyAthletes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPlansForAthlete(AthleteBean athleteBean) {
        try {
            Athlete model = new Athlete();
            model.setId(athleteBean.getId());

            List<WorkoutPlan> plans = workoutPlanDAO.findByAthlete(model);

            // QUI ERA IL BUG: Ora usiamo un metodo completo per convertire anche gli esercizi!
            List<WorkoutPlanBean> beans = convertModelsToBeans(plans);

            view.showAthletePlans(beans);

        } catch (Exception e) {
            e.printStackTrace();
            view.showError("Errore caricamento schede.");
        }
    }

    public void createNewPlan() {
        AthleteBean selectedAthlete = view.getSelectedAthlete();
        if (selectedAthlete != null) {
            System.out.println("Creazione scheda per: " + selectedAthlete.getUsername());
            // Ora chiamiamo il metodo corretto
            ApplicationController.getInstance().loadWorkoutBuilderForClient(selectedAthlete);
        } else {
            view.showError("Seleziona un cliente dalla lista!");
        }
    }

    public void modifySelectedPlan() {
        WorkoutPlanBean plan = view.getSelectedPlan();
        // RECUPERIAMO IL CLIENTE ATTUALMENTE SELEZIONATO NELLA VISTA
        AthleteBean currentClient = view.getSelectedAthlete();

        if (plan != null) {
            System.out.println("Modifica scheda: " + plan.getName());

            // FIX: Passiamo sia la scheda SIA il cliente all'ApplicationController
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

    // --- HELPER DI CONVERSIONE COMPLETO (Copia da NavigationController) ---
    private List<WorkoutPlanBean> convertModelsToBeans(List<WorkoutPlan> models) {
        List<WorkoutPlanBean> beans = new ArrayList<>();
        if (models == null) return beans;

        for (WorkoutPlan plan : models) {
            WorkoutPlanBean bean = new WorkoutPlanBean();
            bean.setId(plan.getId());
            bean.setName(plan.getName());
            bean.setComment(plan.getComment());

            // CONVERSIONE ESERCIZI (Il pezzo mancante!)
            List<WorkoutExerciseBean> exerciseBeans = new ArrayList<>();
            if (plan.getExercises() != null) {
                for (WorkoutExercise modelEx : plan.getExercises()) {
                    WorkoutExerciseBean exBean = new WorkoutExerciseBean();

                    if (modelEx.getExercise() != null) {
                        exBean.setExerciseName(modelEx.getExercise().getName());
                        // Recupera muscolo se presente
                        if (modelEx.getExercise().getMuscleGroup() != null) {
                            exBean.setMuscleGroup(modelEx.getExercise().getMuscleGroup().name());
                        }
                    } else {
                        exBean.setExerciseName("Sconosciuto");
                    }

                    exBean.setSets(modelEx.getSets());
                    exBean.setReps(modelEx.getReps());
                    exBean.setRestTime(modelEx.getRestTime());
                    exerciseBeans.add(exBean);
                }
            }
            bean.setExerciseList(exerciseBeans);
            beans.add(bean);
        }
        return beans;
    }
    }
