package com.example.mygymbro.views.gui;

import com.example.mygymbro.bean.ExerciseBean;
import com.example.mygymbro.bean.WorkoutExerciseBean;
import com.example.mygymbro.bean.WorkoutPlanBean;
import com.example.mygymbro.controller.PlanManagerController;
import com.example.mygymbro.views.WorkoutBuilderView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GraphicWorkoutBuilderView implements WorkoutBuilderView, GraphicView {

    @FXML private TextField txtPlanName;
    @FXML private TextArea txtComment;
    @FXML private ComboBox<ExerciseBean> comboExercises; // Il menu a tendina
    @FXML private TextField txtSets, txtReps, txtRest;
    @FXML private TableView<WorkoutExerciseBean> tableExercises;
    @FXML private TextField txtSearchExercise;
    @FXML private Label lblTotalTime;

    // Colonne Tabella
    @FXML private TableColumn<WorkoutExerciseBean, String> colName;
    @FXML private TableColumn<WorkoutExerciseBean, Integer> colSets;
    @FXML private TableColumn<WorkoutExerciseBean, Integer> colReps;
    @FXML private TableColumn<WorkoutExerciseBean, Integer> colRest;
    @FXML private TableColumn<WorkoutExerciseBean, String> colMuscle;
    @FXML private TableColumn<WorkoutExerciseBean, Void> colDelete;

    private PlanManagerController listener;
    private List<ExerciseBean> allExercisesCache = new ArrayList<>();
    private Parent root;

    @FXML
    public void initialize() {
        // 1. Rendi la tabella editabile
        tableExercises.setEditable(true);

        // Configurazione Colonne
        colName.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        colMuscle.setCellValueFactory(new PropertyValueFactory<>("muscleGroup"));

        // 2. CONFIGURA SETS (Editabile)
        colSets.setCellValueFactory(new PropertyValueFactory<>("sets"));
        colSets.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colSets.setOnEditCommit(event -> {
            WorkoutExerciseBean row = event.getRowValue();
            row.setSets(event.getNewValue());
        });

        // 3. CONFIGURA REPS (Editabile)
        colReps.setCellValueFactory(new PropertyValueFactory<>("reps"));
        colReps.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colReps.setOnEditCommit(event -> {
            WorkoutExerciseBean row = event.getRowValue();
            row.setReps(event.getNewValue());
        });

        // 4. CONFIGURA REST (Editabile)
        colRest.setCellValueFactory(new PropertyValueFactory<>("restTime"));
        colRest.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colRest.setOnEditCommit(event -> {
            WorkoutExerciseBean row = event.getRowValue();
            row.setRestTime(event.getNewValue());
        });

        // 5. FILTRO RICERCA ESERCIZI
        if (txtSearchExercise != null) {
            txtSearchExercise.textProperty().addListener((observable, oldValue, newValue) -> {
                performLiveSearch(newValue);
            });
        }

        // 6. CONFIGURA COLONNA ELIMINA (Bottone X)
        setupDeleteColumn();
    }

    private void setupDeleteColumn() {
        javafx.util.Callback<TableColumn<WorkoutExerciseBean, Void>, TableCell<WorkoutExerciseBean, Void>> cellFactory = new javafx.util.Callback<>() {
            @Override
            public TableCell<WorkoutExerciseBean, Void> call(final TableColumn<WorkoutExerciseBean, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("X");
                    {
                        btn.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                        btn.setOnAction(event -> {
                            WorkoutExerciseBean exercise = getTableView().getItems().get(getIndex());
                            if (listener != null) {
                                listener.removeExerciseFromPlan(exercise);
                            }
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        };
        colDelete.setCellFactory(cellFactory);
    }

    // --- LOGICA DI RICERCA LIVE (API) ---
    private void performLiveSearch(String query) {
        if (listener == null) return;
        new Thread(() -> {
            List<ExerciseBean> results = listener.searchExercisesOnApi(query);
            javafx.application.Platform.runLater(() -> {
                comboExercises.setItems(FXCollections.observableArrayList(results));
                if (!results.isEmpty()) {
                    comboExercises.show();
                }
            });
        }).start();
    }

    // --- AZIONI BOTTONI FXML ---

    @FXML
    public void onAddExercise() {
        ExerciseBean selected = comboExercises.getValue();
        if (selected == null) {
            showError("Seleziona un esercizio!");
            return;
        }

        try {
            int sets = Integer.parseInt(txtSets.getText());
            int reps = Integer.parseInt(txtReps.getText());
            int rest = Integer.parseInt(txtRest.getText());

            // Creiamo il Bean della riga
            WorkoutExerciseBean row = new WorkoutExerciseBean();
            row.setExerciseName(selected.getName());
            row.setMuscleGroup(selected.getMuscleGroup());
            row.setSets(sets);
            row.setReps(reps);
            row.setRestTime(rest);

            // Passiamo al Controller che calcolerà il tempo e aggiornerà la lista
            if (listener != null) {
                listener.addExerciseToPlan(row);
            }

            // Pulizia campi
            txtSets.clear();
            txtReps.clear();
            txtRest.clear();

        } catch (NumberFormatException e) {
            showError("Inserisci numeri validi per Sets, Reps e Rest.");
        }
    }

    @FXML
    public void onSavePlan() {
        if (listener != null) listener.handleSavePlan();
    }

    @FXML
    public void onCancel() {
        if (listener != null) listener.handleCancel();
    }

    // --- IMPLEMENTAZIONE INTERFACCIA WorkoutBuilderView ---

    @Override
    public void setListener(PlanManagerController listener) {
        this.listener = listener;
    }

    @Override
    public String getPlanName() {
        return txtPlanName.getText();
    }

    @Override
    public void setPlanName(String name) {
        this.txtPlanName.setText(name);
    }

    @Override
    public String getComment() {
        return txtComment.getText();
    }

    @Override
    public void setPlanComment(String comment) {
        this.txtComment.setText(comment);
    }

    @Override
    public void populateExerciseMenu(List<ExerciseBean> exercises) {
        this.allExercisesCache = exercises;
        comboExercises.setItems(FXCollections.observableArrayList(exercises));

        // Converter per mostrare solo il nome nella tendina
        comboExercises.setConverter(new StringConverter<>() {
            @Override
            public String toString(ExerciseBean object) {
                return object != null ? object.getName() : "";
            }
            @Override
            public ExerciseBean fromString(String string) {
                return null;
            }
        });
    }

    @Override
    public void updateExerciseTable(List<WorkoutExerciseBean> exercises) {
        if (exercises != null) {
            tableExercises.setItems(FXCollections.observableArrayList(exercises));
        } else {
            tableExercises.getItems().clear();
        }
    }

    @Override
    public void updateExerciseList(List<WorkoutExerciseBean> exercises) {
        // Errore era: listExercises.getItems()... ma la tua tabella si chiama tableExercises!
        Platform.runLater(() -> {
            if (exercises != null) {
                tableExercises.setItems(FXCollections.observableArrayList(exercises));
            } else {
                tableExercises.getItems().clear();
            }
        });
    }

    @Override
    public WorkoutPlanBean getWorkoutPlanBean() {
        WorkoutPlanBean bean = new WorkoutPlanBean();
        bean.setName(txtPlanName.getText());
        bean.setComment(txtComment.getText());
        // Prende gli esercizi direttamente dalla tabella
        bean.setExerciseList(new ArrayList<>(tableExercises.getItems()));
        return bean;
    }

    @Override
    public List<WorkoutExerciseBean> getAddedExercises() {
        return tableExercises.getItems();
    }

    @Override
    public void updateTotalTime(String timeMessage) {
        if (lblTotalTime != null) {
            javafx.application.Platform.runLater(() -> lblTotalTime.setText(timeMessage));
        }
    }

    // --- IMPLEMENTAZIONE INTERFACCIA View (Messaggi) ---

    @Override
    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Errore");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @Override
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Successo");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- IMPLEMENTAZIONE GraphicView ---

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public void setRoot(Parent root) {
        this.root = root;
    }
}