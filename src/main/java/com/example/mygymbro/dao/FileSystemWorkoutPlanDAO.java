package com.example.mygymbro.dao;

import com.example.mygymbro.exceptions.DAOException;
import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.WorkoutPlan;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSystemWorkoutPlanDAO implements WorkoutPlanDAO {

    private static final Logger LOGGER = Logger.getLogger(FileSystemWorkoutPlanDAO.class.getName());
    private static final String JSON_EXT = ".json";

    // Costruiamo il percorso in modo sicuro per ogni sistema operativo
    private static final String DATA_DIR_PATH = System.getProperty("user.home") + File.separator + "mygymbro_data" + File.separator + "plans";

    private final Gson gson;
    private final File dataDir;

    public FileSystemWorkoutPlanDAO() {
        this.gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
        this.dataDir = new File(DATA_DIR_PATH);

        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (!created) {
                LOGGER.log(Level.WARNING, "Impossibile creare la cartella dati: {0}", DATA_DIR_PATH);
            }
        }
    }

    @Override
    public void save(WorkoutPlan plan) throws DAOException {
        if (plan.getId() == 0) {
            // Generiamo un ID semplice basato sul tempo (secondi)
            plan.setId((int) (System.currentTimeMillis() / 1000));
        }

        File file = new File(dataDir, plan.getId() + JSON_EXT);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(plan, writer);
        } catch (IOException e) {
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
        File file = new File(dataDir, planId + JSON_EXT);
        if (file.exists() && !file.delete()) {
            throw new DAOException("Impossibile eliminare il file: " + planId, new IOException("File.delete() ha restituito false"));
        }
    }

    @Override
    public List<WorkoutPlan> findByAthlete(Athlete athlete) throws DAOException {
        List<WorkoutPlan> result = new ArrayList<>();

        // Se la cartella non esiste o è vuota, ritorna lista vuota
        if (!dataDir.exists()) return result;

        File[] listOfFiles = dataDir.listFiles();
        if (listOfFiles == null) return result;

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(JSON_EXT)) {
                try (FileReader reader = new FileReader(file)) {
                    WorkoutPlan plan = gson.fromJson(reader, WorkoutPlan.class);

                    // Controlliamo se l'atleta corrisponde (tramite oggetto O tramite ID se presente)
                    if (isAthleteMatch(plan, athlete)) {
                        result.add(plan);
                    }

                } catch (IOException e) {
                    throw new DAOException("Errore nella lettura del file: " + file.getName(), e);
                }
            }
        }
        return result;
    }

    private boolean isAthleteMatch(WorkoutPlan plan, Athlete athlete) {
        boolean matchByObject = plan.getAthlete() != null && plan.getAthlete().getId() == athlete.getId();
        boolean matchById = plan.getAthleteId() == athlete.getId();
        return matchByObject || matchById;
    }
}