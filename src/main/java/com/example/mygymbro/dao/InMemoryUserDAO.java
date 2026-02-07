package com.example.mygymbro.dao;

import com.example.mygymbro.model.Athlete;
import com.example.mygymbro.model.PersonalTrainer;
import com.example.mygymbro.model.User;
import java.util.ArrayList;
import java.util.List;

public class InMemoryUserDAO implements UserDAO {

    // LISTA STATICA: Sopravvive finché l'app è aperta
    public static List<User> ramDB = new ArrayList<>();

    // BLOCCO STATICO: Viene eseguito una volta sola all'avvio
    static {
        // Creiamo l'utente DEMO per accedere subito
        Athlete demoUser = new Athlete();
        demoUser.setId(1); // ID fisso
        demoUser.setUsername("demo"); // <--- USERNAME FACILE
        demoUser.setPassword("demo"); // <--- PASSWORD FACILE
        demoUser.setName("Mario");
        demoUser.setCognome("Rossi");
        demoUser.setEmail("demo@mygymbro.it");
        ramDB.add(demoUser);

        PersonalTrainer demoPT = new PersonalTrainer(2, "trainer", "trainer", "Carlo", "Verdone", "carlo@gym.it", "FIT-CERT-001");
        ramDB.add(demoPT);
    }

    @Override
    public User findByUsername(String username) {
        return ramDB.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username)) // IgnoreCase è più comodo
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(User user) {
        // Simulazione salvataggio
        if (findByUsername(user.getUsername()) == null) {
            // Assegniamo un ID se non ce l'ha
            if (user.getId() == 0) {
                user.setId(ramDB.size() + 1);
            }
            ramDB.add(user);
        }
    }
}