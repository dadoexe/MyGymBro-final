package com.example.mygymbro.views.cli;

import com.example.mygymbro.views.View;

public interface CliView extends View {
    // Questo è il metodo che sostituisce il vecchio "show()" per la console
    void run();
}