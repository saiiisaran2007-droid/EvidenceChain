package com.evidencechain.controller;

import com.evidencechain.model.CustodyEvent;
import com.evidencechain.model.Evidence;
import com.evidencechain.service.EvidenceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

/**
 * View for Feature 3: Chain of Custody System-Wide Audit Log and Event Logging.
 */
public class CustodyView {

    private final EvidenceService evidenceService;
    private final ObservableList<CustodyEvent> eventList = FXCollections.observableArrayList();

    private TableView<CustodyEvent> custodyTable;
    private TextField searchField;
    private Label countLabel;

    public CustodyView(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    public Node createView() {
        VBox root = new VBox(18);

        // Header and Controls
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("🔍 Filter by Evidence ID, Action, Handler...");
        searchField.setPrefWidth(340);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        countLabel = new Label("Total: 0 events");
        countLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadData());

        Button addEventBtn = new Button("⇄ Record Custody Transfer");
        addEventBtn.getStyleClass().add("btn-primary");
        addEventBtn.setOnAction(e -> openRecordEventDialog());

        topRow.getChildren().addAll(searchField, spacer, countLabel, refreshBtn, addEventBtn);

        // Custody Table
        custodyTable = new TableView<>();
        custodyTable.setPlaceholder(new Label("No custody events recorded in SQLite yet."));
        VBox.setVgrow(custodyTable, Priority.ALWAYS);

        TableColumn<CustodyEvent, String> idCol = new TableColumn<>("EVENT #");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getEventId())));
        idCol.setPrefWidth(80);

        TableColumn<CustodyEvent, String> evidenceIdCol = new TableColumn<>("EVIDENCE ID");
        evidenceIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEvidenceId()));
        evidenceIdCol.setPrefWidth(120);

        TableColumn<CustodyEvent, String> actionCol = new TableColumn<>("ACTION");
        actionCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(action);
                    badge.setStyle(getActionStyle(action));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<CustodyEvent, String> fromCol = new TableColumn<>("FROM / RELINQUISHED BY");
        fromCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActorFrom()));
        fromCol.setPrefWidth(180);

        TableColumn<CustodyEvent, String> toCol = new TableColumn<>("TO / RECEIVED BY");
        toCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActorTo()));
        toCol.setPrefWidth(180);

        TableColumn<CustodyEvent, String> timeCol = new TableColumn<>("TIMESTAMP");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp()));
        timeCol.setPrefWidth(160);

        TableColumn<CustodyEvent, String> notesCol = new TableColumn<>("NOTES / LOCATION");
        notesCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotes()));
        notesCol.setPrefWidth(260);

        custodyTable.getColumns().addAll(idCol, evidenceIdCol, actionCol, fromCol, toCol, timeCol, notesCol);

        // Search Filter
        FilteredList<CustodyEvent> filteredData = new FilteredList<>(eventList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(ev -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    return true;
                }
                String lower = newVal.toLowerCase().trim();
                return (ev.getEvidenceId() != null && ev.getEvidenceId().toLowerCase().contains(lower)) ||
                       (ev.getAction() != null && ev.getAction().toLowerCase().contains(lower)) ||
                       (ev.getActorFrom() != null && ev.getActorFrom().toLowerCase().contains(lower)) ||
                       (ev.getActorTo() != null && ev.getActorTo().toLowerCase().contains(lower)) ||
                       (ev.getNotes() != null && ev.getNotes().toLowerCase().contains(lower));
            });
            countLabel.setText("Showing: " + filteredData.size() + " of " + eventList.size() + " events");
        });

        custodyTable.setItems(filteredData);
        loadData();

        root.getChildren().addAll(topRow, custodyTable);
        return root;
    }

    private void loadData() {
        try {
            List<CustodyEvent> all = evidenceService.getRecentCustodyEvents(100);
            eventList.setAll(all);
            countLabel.setText("Total: " + all.size() + " events");
        } catch (SQLException e) {
            System.err.println("[CustodyView] Failed to query custody events: " + e.getMessage());
        }
    }

    private void openRecordEventDialog() {
        List<Evidence> evidenceList;
        try {
            evidenceList = evidenceService.getAllEvidence();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if (evidenceList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No evidence registered yet. Please register evidence first.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Record Chain of Custody Event");
        dialog.setHeaderText("Create custody transaction and commit linked block to ledger");

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("soc-card");
        pane.setStyle("-fx-background-color: #0f172a; -fx-padding: 16px;");

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(14);
        form.setPadding(new Insets(10));

        ComboBox<String> evidenceCombo = new ComboBox<>();
        for (Evidence ev : evidenceList) {
            evidenceCombo.getItems().add(ev.getEvidenceId() + " (" + ev.getCaseId() + " - " + ev.getEvidenceType() + ")");
        }
        evidenceCombo.getSelectionModel().selectFirst();
        evidenceCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> actionCombo = new ComboBox<>();
        actionCombo.getItems().addAll("TRANSFERRED", "ANALYSIS STARTED", "RETURNED", "REGISTERED");
        actionCombo.getSelectionModel().select("TRANSFERRED");
        actionCombo.setMaxWidth(Double.MAX_VALUE);

        TextField fromField = new TextField();
        fromField.setPromptText("Current analyst / user");

        TextField toField = new TextField();
        toField.setPromptText("Receiving analyst / facility");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Reason for transfer, secure vault location, or lab notes...");
        notesArea.setPrefRowCount(3);

        form.add(new Label("SELECT EVIDENCE ITEM *"), 0, 0);
        form.add(evidenceCombo, 1, 0);

        form.add(new Label("CUSTODY ACTION *"), 0, 1);
        form.add(actionCombo, 1, 1);

        form.add(new Label("FROM (RELINQUISHING USER) *"), 0, 2);
        form.add(fromField, 1, 2);

        form.add(new Label("TO (RECEIVING USER) *"), 0, 3);
        form.add(toField, 1, 3);

        form.add(new Label("NOTES / FACILITY LOCATION"), 0, 4);
        form.add(notesArea, 1, 4);

        pane.setContent(form);

        ButtonType submitType = new ButtonType("Commit Custody Event", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        Button submitBtn = (Button) pane.lookupButton(submitType);
        submitBtn.getStyleClass().add("btn-primary");

        dialog.setResultConverter(btn -> {
            if (btn == submitType) {
                String selectedRaw = evidenceCombo.getValue();
                String evidenceId = selectedRaw.split(" ")[0];
                String action = actionCombo.getValue();
                String from = fromField.getText();
                String to = toField.getText();
                String notes = notesArea.getText();

                if (from == null || from.trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "From user is required.", ButtonType.OK);
                    alert.showAndWait();
                    return false;
                }

                try {
                    evidenceService.addCustodyEvent(evidenceId, action, from, to, notes);
                    return true;
                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save custody event: " + ex.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadData();
            }
        });
    }

    private String getActionStyle(String action) {
        return switch (action) {
            case "REGISTERED" -> "-fx-background-color: rgba(6, 182, 212, 0.2); -fx-text-fill: #22d3ee; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "TRANSFERRED" -> "-fx-background-color: rgba(59, 130, 246, 0.2); -fx-text-fill: #60a5fa; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "ANALYSIS STARTED" -> "-fx-background-color: rgba(245, 158, 11, 0.2); -fx-text-fill: #fbbf24; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "RETURNED" -> "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #34d399; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            default -> "-fx-background-color: rgba(148, 163, 184, 0.2); -fx-text-fill: #cbd5e1; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;";
        };
    }
}
