package com.example.mygymbro.views.gui;

import javafx.scene.Parent;

public interface GraphicView {
    // Permette al Controller di ottenere il layout grafico caricato dall'FXML
    Parent getRoot();

    // Permette alla Factory di impostare il layout appena caricato
    void setRoot(Parent root);
}