package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException; // Import fondamentale
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.WorkoutPlan;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileSystemWorkoutPlanDAO implements WorkoutPlanDAO {

    private static final String DATA_DIR = System.getProperty("user.home") + File.separator + "mygymbro_data" + File.separator + "plans";
    private final Gson gson;

    public FileSystemWorkoutPlanDAO() {
        this.gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public void save(WorkoutPlan plan) throws DAOException {
        if (plan.getId() == 0) {
            plan.setId((int) (System.currentTimeMillis() / 1000));
        }

        File file = new File(DATA_DIR, plan.getId() + ".json");

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(plan, writer);
        } catch (IOException e) {
            // CATTURIAMO l'errore tecnico (IOException) e lanciamo quello del progetto (DAOException)
            throw new DAOException("Impossibile salvare il file della scheda su disco", e);
        }
    }

    @Override
    public void update(WorkoutPlan plan) throws DAOException {
        // Su File System, sovrascrivere è uguale ad aggiornare
        save(plan);
    }

    @Override
    public void delete(int planId) throws DAOException {
        File file = new File(DATA_DIR, planId + ".json");
        if (file.exists()) {
            if (!file.delete()) {
                // Generiamo un errore fittizio per soddisfare il costruttore (Messaggio, Causa)
                throw new DAOException("Impossibile eliminare il file: " + planId, new IOException("File.delete() ha restituito false"));
            }
        }
    }

    @Override
    public List<WorkoutPlan> findByAthlete(Athlete athlete) throws DAOException {
        List<WorkoutPlan> result = new ArrayList<>();
        File folder = new File(DATA_DIR);

        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    try (FileReader reader = new FileReader(file)) {
                        WorkoutPlan plan = gson.fromJson(reader, WorkoutPlan.class);

                        // Filtro per atleta
                        if (plan.getAthlete() != null && plan.getAthlete().getId() == athlete.getId()) {
                            result.add(plan);
                        } else if (plan.getAthleteId() == athlete.getId()) {
                            result.add(plan);
                        }

                    } catch (IOException e) {
                        // Se un file è corrotto, lanciamo l'errore DAO
                        throw new DAOException("Errore nella lettura del file: " + file.getName(), e);
                    }
                }
            }
        }
        return result;
    }
}