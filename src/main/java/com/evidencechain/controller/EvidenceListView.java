package com.evidencechain.controller;

import com.evidencechain.model.Evidence;
import com.evidencechain.service.EvidenceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * View displaying the live catalog of registered digital evidence items from SQLite.
 * Features required columns: Evidence ID, Case ID, Type, Collector, Status, Timestamp.
 */
public class EvidenceListView {

    private final EvidenceService evidenceService;
    private final Consumer<Evidence> onSelectEvidenceDetails;
    private final Runnable onNavigateToRegister;

    private final ObservableList<Evidence> evidenceMasterList = FXCollections.observableArrayList();
    private TableView<Evidence> tableView;
    private TextField searchField;
    private Label recordCountLabel;

    public EvidenceListView(EvidenceService evidenceService,
                            Consumer<Evidence> onSelectEvidenceDetails,
                            Runnable onNavigateToRegister) {
        this.evidenceService = evidenceService;
        this.onSelectEvidenceDetails = onSelectEvidenceDetails;
        this.onNavigateToRegister = onNavigateToRegister;
    }

    public Node createView() {
        VBox root = new VBox(16);

        // Top Toolbar: Search + Action buttons
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search by Evidence ID, Case ID, Type, Collector...");
        searchField.setPrefWidth(380);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        recordCountLabel = new Label("Total: 0 records");
        recordCountLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadDataFromDatabase());

        Button registerBtn = new Button("⊕ Register Evidence");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setOnAction(e -> {
            if (onNavigateToRegister != null) {
                onNavigateToRegister.run();
            }
        });

        toolbar.getChildren().addAll(searchField, spacer, recordCountLabel, refreshBtn, registerBtn);

        // TableView setup
        tableView = new TableView<>();
        tableView.setPlaceholder(new Label("No digital evidence registered yet in SQLite.\nClick '⊕ Register Evidence' above to register the first item."));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // 1. EVIDENCE ID
        TableColumn<Evidence, String> idCol = new TableColumn<>("EVIDENCE ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEvidenceId()));
        idCol.setPrefWidth(120);

        // 2. CASE ID
        TableColumn<Evidence, String> caseCol = new TableColumn<>("CASE ID");
        caseCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCaseId()));
        caseCol.setPrefWidth(140);

        // 3. TYPE
        TableColumn<Evidence, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEvidenceType()));
        typeCol.setPrefWidth(170);

        // 4. COLLECTOR
        TableColumn<Evidence, String> collectorCol = new TableColumn<>("COLLECTOR");
        collectorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCollector()));
        collectorCol.setPrefWidth(150);

        // 5. STATUS (Latest custody action / state badge)
        TableColumn<Evidence, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(data -> {
            String status = evidenceService.getLatestEvidenceStatus(data.getValue().getEvidenceId());
            return new SimpleStringProperty(status);
        });
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label("● " + status);
                    badge.setStyle(getStatusBadgeStyle(status));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // 6. TIMESTAMP
        TableColumn<Evidence, String> dateCol = new TableColumn<>("TIMESTAMP");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRecordedAt()));
        dateCol.setPrefWidth(160);

        // 7. ACTIONS (Details button)
        TableColumn<Evidence, Void> actionCol = new TableColumn<>("ACTIONS");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("Details →");
            {
                viewBtn.getStyleClass().add("btn-secondary");
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3px 10px;");
                viewBtn.setOnAction(e -> {
                    Evidence item = getTableView().getItems().get(getIndex());
                    if (onSelectEvidenceDetails != null) {
                        onSelectEvidenceDetails.accept(item);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });

        tableView.getColumns().addAll(idCol, caseCol, typeCol, collectorCol, statusCol, dateCol, actionCol);

        // Double-click row handler to view details
        tableView.setRowFactory(tv -> {
            TableRow<Evidence> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Evidence rowData = row.getItem();
                    if (onSelectEvidenceDetails != null) {
                        onSelectEvidenceDetails.accept(rowData);
                    }
                }
            });
            return row;
        });

        // Filtered List for Search
        FilteredList<Evidence> filteredData = new FilteredList<>(evidenceMasterList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(evidence -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lower = newValue.toLowerCase().trim();
                return (evidence.getEvidenceId() != null && evidence.getEvidenceId().toLowerCase().contains(lower)) ||
                       (evidence.getCaseId() != null && evidence.getCaseId().toLowerCase().contains(lower)) ||
                       (evidence.getEvidenceType() != null && evidence.getEvidenceType().toLowerCase().contains(lower)) ||
                       (evidence.getCollector() != null && evidence.getCollector().toLowerCase().contains(lower)) ||
                       (evidence.getSourceDevice() != null && evidence.getSourceDevice().toLowerCase().contains(lower));
            });
            recordCountLabel.setText("Showing: " + filteredData.size() + " of " + evidenceMasterList.size() + " records");
        });

        tableView.setItems(filteredData);

        // Load initial records from database
        loadDataFromDatabase();

        root.getChildren().addAll(toolbar, tableView);
        return root;
    }

    public void loadDataFromDatabase() {
        try {
            List<Evidence> list = evidenceService.getAllEvidence();
            evidenceMasterList.setAll(list);
            recordCountLabel.setText("Total: " + list.size() + " records");
        } catch (SQLException e) {
            recordCountLabel.setText("Error loading records: " + e.getMessage());
            System.err.println("[EvidenceListView] Database error loading evidence: " + e.getMessage());
        }
    }

    private String getStatusBadgeStyle(String status) {
        return switch (status) {
            case "REGISTERED" -> "-fx-background-color: rgba(6, 182, 212, 0.15); -fx-text-fill: #22d3ee; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "TRANSFERRED" -> "-fx-background-color: rgba(59, 130, 246, 0.15); -fx-text-fill: #60a5fa; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "ANALYSIS STARTED" -> "-fx-background-color: rgba(245, 158, 11, 0.15); -fx-text-fill: #fbbf24; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "RETURNED" -> "-fx-background-color: rgba(16, 185, 129, 0.15); -fx-text-fill: #34d399; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            default -> "-fx-background-color: rgba(148, 163, 184, 0.15); -fx-text-fill: #cbd5e1; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;";
        };
    }
}
