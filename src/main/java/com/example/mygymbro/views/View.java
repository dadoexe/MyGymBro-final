package com.example.mygymbro.views;

public interface View {
    // Unico metodo per mostrare messaggi di successo/info (es. "Salvataggio ok")
    void showSuccess(String message);

    // Unico metodo per mostrare errori (es. "Campo obbligatorio")
    void showError(String message);
}