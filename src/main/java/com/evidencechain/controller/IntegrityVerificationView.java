package com.evidencechain.controller;

import com.evidencechain.model.Evidence;
import com.evidencechain.model.IntegrityVerificationResult;
import com.evidencechain.service.EvidenceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * View for Feature 5: Cryptographic Integrity Verification dashboard.
 * Audits registered evidence files against actual files on disk using SHA-256.
 */
public class IntegrityVerificationView {

    private final EvidenceService evidenceService;
    private final Consumer<Evidence> onInspectEvidence;

    private final ObservableList<Evidence> evidenceList = FXCollections.observableArrayList();
    private final Map<String, IntegrityVerificationResult> verificationResults = new HashMap<>();

    private TableView<Evidence> auditTable;
    private Label totalEvidenceLabel;
    private Label verifiedCountLabel;
    private Label tamperedCountLabel;

    public IntegrityVerificationView(EvidenceService evidenceService, Consumer<Evidence> onInspectEvidence) {
        this.evidenceService = evidenceService;
        this.onInspectEvidence = onInspectEvidence;
    }

    public Node createView() {
        VBox root = new VBox(18);

        // Header and Controls
        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("CRYPTOGRAPHIC INTEGRITY VERIFICATION");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Recomputes SHA-256 directly from disk files and compares with immutable stored signatures.");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button verifyAllBtn = new Button("✓ Audit All Evidence Items");
        verifyAllBtn.getStyleClass().add("btn-primary");
        verifyAllBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 18px;");
        verifyAllBtn.setOnAction(e -> verifyAll());

        Button refreshBtn = new Button("↻ Reload");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadData());

        topRow.getChildren().addAll(titleBox, spacer, refreshBtn, verifyAllBtn);

        // Stats Cards
        HBox statsRow = new HBox(16);
        totalEvidenceLabel = new Label("0");
        totalEvidenceLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 20px; -fx-font-weight: bold;");

        verifiedCountLabel = new Label("0");
        verifiedCountLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 20px; -fx-font-weight: bold;");

        tamperedCountLabel = new Label("0");
        tamperedCountLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 20px; -fx-font-weight: bold;");

        statsRow.getChildren().addAll(
                createStatCard("REGISTERED ITEMS", totalEvidenceLabel, "#3b82f6"),
                createStatCard("VERIFIED INTACT", verifiedCountLabel, "#10b981"),
                createStatCard("TAMPERED / MISMATCHED", tamperedCountLabel, "#ef4444")
        );

        // TableView
        auditTable = new TableView<>();
        auditTable.setPlaceholder(new Label("No evidence items registered in SQLite to verify."));
        VBox.setVgrow(auditTable, Priority.ALWAYS);

        TableColumn<Evidence, String> idCol = new TableColumn<>("EVIDENCE ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEvidenceId()));
        idCol.setPrefWidth(110);

        TableColumn<Evidence, String> caseCol = new TableColumn<>("CASE ID");
        caseCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCaseId()));
        caseCol.setPrefWidth(130);

        TableColumn<Evidence, String> pathCol = new TableColumn<>("FILE PATH");
        pathCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFilePath()));
        pathCol.setPrefWidth(240);

        TableColumn<Evidence, String> storedHashCol = new TableColumn<>("STORED SHA-256");
        storedHashCol.setCellValueFactory(d -> {
            String h = d.getValue().getSha256Hash();
            return new SimpleStringProperty(h != null && h.length() > 16 ? h.substring(0, 10) + "..." + h.substring(h.length() - 8) : h);
        });
        storedHashCol.setPrefWidth(170);

        TableColumn<Evidence, String> statusCol = new TableColumn<>("AUDIT STATUS");
        statusCol.setCellValueFactory(d -> {
            IntegrityVerificationResult res = verificationResults.get(d.getValue().getEvidenceId());
            if (res == null) {
                return new SimpleStringProperty("NOT AUDITED");
            }
            return new SimpleStringProperty(res.isVerified() ? "INTEGRITY VERIFIED" : "TAMPER DETECTED");
        });
        statusCol.setPrefWidth(170);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else if ("INTEGRITY VERIFIED".equals(status)) {
                    Label badge = new Label("✓ INTEGRITY VERIFIED");
                    badge.setStyle("-fx-background-color: #059669; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                } else if ("TAMPER DETECTED".equals(status)) {
                    Label badge = new Label("⚠ TAMPER DETECTED");
                    badge.setStyle("-fx-background-color: #dc2626; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                } else {
                    Label badge = new Label("● PENDING AUDIT");
                    badge.setStyle("-fx-background-color: rgba(148, 163, 184, 0.15); -fx-text-fill: #94a3b8; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Evidence, Void> actionCol = new TableColumn<>("ACTIONS");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button verifySingleBtn = new Button("Verify");
            private final Button inspectBtn = new Button("Inspect →");
            private final HBox btnBox = new HBox(6, verifySingleBtn, inspectBtn);
            {
                verifySingleBtn.getStyleClass().add("btn-primary");
                verifySingleBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                verifySingleBtn.setOnAction(e -> {
                    Evidence item = getTableView().getItems().get(getIndex());
                    verifySingle(item);
                });

                inspectBtn.getStyleClass().add("btn-secondary");
                inspectBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                inspectBtn.setOnAction(e -> {
                    Evidence item = getTableView().getItems().get(getIndex());
                    if (onInspectEvidence != null) {
                        onInspectEvidence.accept(item);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnBox);
                }
            }
        });

        auditTable.getColumns().addAll(idCol, caseCol, pathCol, storedHashCol, statusCol, actionCol);

        loadData();

        root.getChildren().addAll(topRow, statsRow, auditTable);
        return root;
    }

    private void loadData() {
        try {
            List<Evidence> list = evidenceService.getAllEvidence();
            evidenceList.setAll(list);
            auditTable.setItems(evidenceList);
            totalEvidenceLabel.setText(String.valueOf(list.size()));
            updateCounts();
        } catch (SQLException e) {
            System.err.println("[IntegrityVerificationView] Failed to load evidence: " + e.getMessage());
        }
    }

    private void verifySingle(Evidence evidence) {
        IntegrityVerificationResult res = evidenceService.verifyEvidenceIntegrity(evidence);
        verificationResults.put(evidence.getEvidenceId(), res);
        auditTable.refresh();
        updateCounts();
    }

    private void verifyAll() {
        for (Evidence ev : evidenceList) {
            IntegrityVerificationResult res = evidenceService.verifyEvidenceIntegrity(ev);
            verificationResults.put(ev.getEvidenceId(), res);
        }
        auditTable.refresh();
        updateCounts();
    }

    private void updateCounts() {
        int verified = 0;
        int tampered = 0;
        for (IntegrityVerificationResult res : verificationResults.values()) {
            if (res.isVerified()) {
                verified++;
            } else {
                tampered++;
            }
        }
        verifiedCountLabel.setText(String.valueOf(verified));
        tamperedCountLabel.setText(String.valueOf(tampered));
    }

    private VBox createStatCard(String title, Label valueLabel, String accentColor) {
        VBox card = new VBox(6);
        card.getStyleClass().add("soc-card");
        card.setStyle(card.getStyle() + "; -fx-border-color: #1e293b; -fx-border-left-color: " + accentColor + "; -fx-border-left-width: 4px;");
        card.setPrefWidth(240);

        Label t = new Label(title);
        t.getStyleClass().add("soc-card-title");

        card.getChildren().addAll(t, valueLabel);
        return card;
    }
}
