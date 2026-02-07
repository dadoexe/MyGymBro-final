package com.example.mygymbro.dao;

import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.PersonalTrainer;
import com.example.mygymbro.model.User;
import java.util.ArrayList;
import java.util.List;

public class InMemoryUserDAO implements UserDAO {

    // CORREZIONE: private + final (SonarCloud approva)
    private static final List<User> ramDB = new ArrayList<>();

    // BLOCCO STATICO: Viene eseguito una volta sola all'avvio
    static {
        // 1. Utente DEMO (Atleta)
        Athlete demoUser = new Athlete();
        demoUser.setId(1);
        demoUser.setUsername("demo");
        demoUser.setPassword("demo");
        demoUser.setNome("Mario");
        demoUser.setCognome("Rossi");
        demoUser.setEmail("demo@mygymbro.it");
        ramDB.add(demoUser);

        // 2. Utente TRAINER
        PersonalTrainer demoPT = new PersonalTrainer(2, "trainer", "trainer", "Carlo", "Verdone", "carlo@gym.it", "FIT-CERT-001");
        ramDB.add(demoPT);
    }

    // Metodo Getter per permettere ad altri DAO (es. TrainerDAO) di leggere la lista
    public static List<User> getRamDB() {
        return ramDB;
    }

    @Override
    public User findByUsername(String username) {
        return ramDB.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(User user) {
        // Rimuoviamo l'utente se esiste già (per simulare un update)
        ramDB.removeIf(u -> u.getUsername().equals(user.getUsername()));

        // Se è un nuovo utente (ID 0), generiamo un ID
        if (user.getId() == 0) {
            user.setId(ramDB.size() + 1);
        }

        ramDB.add(user);
    }
}