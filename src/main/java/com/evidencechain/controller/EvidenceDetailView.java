package com.evidencechain.controller;

import com.evidencechain.model.CustodyEvent;
import com.evidencechain.model.Evidence;
import com.evidencechain.model.IntegrityVerificationResult;
import com.evidencechain.service.EvidenceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;

/**
 * View showing deep forensic metadata inspection, file integrity verification,
 * and chronological chain-of-custody tracking for a specific evidence item.
 */
public class EvidenceDetailView {

    private final Evidence evidence;
    private final EvidenceService evidenceService;
    private final Runnable onBack;

    private Label integrityBadge;
    private VBox verificationResultBox;
    private TableView<CustodyEvent> custodyTable;
    private Label custodyCountLabel;
    private byte[] originalFileBackup;

    public EvidenceDetailView(Evidence evidence, EvidenceService evidenceService, Runnable onBack) {
        this.evidence = evidence;
        this.evidenceService = evidenceService;
        this.onBack = onBack;
    }

    public Node createView() {
        VBox root = new VBox(20);

        // Header Row: Back button, Badges, and Status
        HBox headerRow = new HBox(14);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back to Evidence Catalog");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });

        Label idBadge = new Label(evidence.getEvidenceId());
        idBadge.setStyle("-fx-background-color: rgba(6, 182, 212, 0.15); -fx-text-fill: #22d3ee; -fx-font-weight: bold; -fx-font-size: 15px; -fx-padding: 5px 14px; -fx-background-radius: 6px; -fx-border-color: #06b6d4; -fx-border-radius: 6px;");

        Label caseBadge = new Label("Case: " + evidence.getCaseId());
        caseBadge.setStyle("-fx-background-color: rgba(59, 130, 246, 0.15); -fx-text-fill: #60a5fa; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 5px 12px; -fx-background-radius: 6px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        File fileOnDisk = new File(evidence.getFilePath());
        boolean fileExists = fileOnDisk.exists() && fileOnDisk.isFile();
        integrityBadge = new Label(fileExists ? "● ON DISK — READY FOR AUDIT" : "⚠ FILE NOT FOUND ON DISK");
        integrityBadge.setStyle(fileExists ?
                "-fx-background-color: rgba(16, 185, 129, 0.15); -fx-text-fill: #34d399; -fx-padding: 6px 14px; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-font-size: 11px;" :
                "-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #f87171; -fx-padding: 6px 14px; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-font-size: 11px;");

        headerRow.getChildren().addAll(backBtn, idBadge, caseBadge, spacer, integrityBadge);

        // FEATURE 5: Integrity Verification Card
        VBox integrityCard = buildIntegrityVerificationCard();

        // Metadata Card
        VBox metaCard = buildMetadataCard();

        // SHA-256 Fingerprint Card
        VBox hashCard = buildFingerprintCard();

        // FEATURE 3: Chain of Custody History Card
        VBox custodyCard = buildCustodyHistoryCard();

        root.getChildren().addAll(headerRow, integrityCard, metaCard, hashCard, custodyCard);
        return root;
    }

    /**
     * Builds the Cryptographic Integrity Verification section.
     */
    private VBox buildIntegrityVerificationCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("soc-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("FEATURE 5: CRYPTOGRAPHIC INTEGRITY VERIFICATION");
        title.getStyleClass().add("soc-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button verifyBtn = new Button("✓ VERIFY EVIDENCE");
        verifyBtn.getStyleClass().add("btn-primary");
        verifyBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 18px;");
        verifyBtn.setOnAction(e -> executeIntegrityVerification());

        header.getChildren().addAll(title, spacer, verifyBtn);

        // Result container
        verificationResultBox = new VBox(8);
        verificationResultBox.setStyle("-fx-background-color: #0b1120; -fx-padding: 14px; -fx-border-color: #1e293b; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Label promptLabel = new Label("Click 'VERIFY EVIDENCE' to compute SHA-256 directly from disk and compare against the stored hash.");
        promptLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        verificationResultBox.getChildren().add(promptLabel);

        // Interactive Tamper Testing controls for Live Demo
        HBox tamperDemoRow = new HBox(12);
        tamperDemoRow.setAlignment(Pos.CENTER_LEFT);
        tamperDemoRow.setPadding(new Insets(8, 0, 0, 0));

        Label demoLabel = new Label("DEMO TAMPER TEST:");
        demoLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");

        Button tamperBtn = new Button("Simulate Tamper (Modify File on Disk)");
        tamperBtn.getStyleClass().add("btn-secondary");
        tamperBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #f87171; -fx-border-color: #7f1d1d;");
        tamperBtn.setOnAction(e -> simulateFileTamper());

        Button restoreBtn = new Button("Restore Original File");
        restoreBtn.getStyleClass().add("btn-secondary");
        restoreBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399; -fx-border-color: #064e3b;");
        restoreBtn.setOnAction(e -> restoreOriginalFile());

        tamperDemoRow.getChildren().addAll(demoLabel, tamperBtn, restoreBtn);

        card.getChildren().addAll(header, verificationResultBox, tamperDemoRow);
        return card;
    }

    private void executeIntegrityVerification() {
        IntegrityVerificationResult result = evidenceService.verifyEvidenceIntegrity(evidence);
        verificationResultBox.getChildren().clear();

        if (result.isVerified()) {
            // GREEN "INTEGRITY VERIFIED"
            VBox box = new VBox(6);
            Label banner = new Label("INTEGRITY VERIFIED");
            banner.setStyle("-fx-background-color: #059669; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8px 14px; -fx-background-radius: 4px;");

            Label msg = new Label("File on disk exactly matches the original stored SHA-256 fingerprint.");
            msg.setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold; -fx-font-size: 12px;");

            Label hashRow = new Label("Validated SHA-256: " + result.getStoredHash());
            hashRow.setStyle("-fx-text-fill: #22d3ee; -fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

            Label timeRow = new Label("Audit Verified At: " + result.getVerifiedAt() + " | Path: " + result.getFilePath());
            timeRow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

            box.getChildren().addAll(banner, msg, hashRow, timeRow);
            verificationResultBox.getChildren().add(box);
            verificationResultBox.setStyle("-fx-background-color: rgba(5, 150, 105, 0.1); -fx-padding: 14px; -fx-border-color: #059669; -fx-border-radius: 6px; -fx-background-radius: 6px;");

            integrityBadge.setText("● INTEGRITY VERIFIED");
            integrityBadge.setStyle("-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #34d399; -fx-padding: 6px 14px; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            // RED "TAMPER DETECTED"
            VBox box = new VBox(6);
            Label banner = new Label("TAMPER DETECTED");
            banner.setStyle("-fx-background-color: #dc2626; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8px 14px; -fx-background-radius: 4px;");

            Label msg = new Label(result.getMessage());
            msg.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold; -fx-font-size: 12px;");

            Label storedHashLbl = new Label("ORIGINAL STORED HASH:   " + result.getStoredHash());
            storedHashLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");

            String calc = result.getCalculatedHash() != null ? result.getCalculatedHash() : "N/A (FILE NOT ACCESSIBLE)";
            Label calcHashLbl = new Label("CALCULATED DISK HASH:  " + calc);
            calcHashLbl.setStyle("-fx-text-fill: #ef4444; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px; -fx-font-weight: bold;");

            Label timeRow = new Label("Tamper Detected At: " + result.getVerifiedAt() + " | Path: " + result.getFilePath());
            timeRow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

            box.getChildren().addAll(banner, msg, storedHashLbl, calcHashLbl, timeRow);
            verificationResultBox.getChildren().add(box);
            verificationResultBox.setStyle("-fx-background-color: rgba(220, 38, 38, 0.1); -fx-padding: 14px; -fx-border-color: #dc2626; -fx-border-radius: 6px; -fx-background-radius: 6px;");

            integrityBadge.setText("⚠ TAMPER DETECTED");
            integrityBadge.setStyle("-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #f87171; -fx-padding: 6px 14px; -fx-background-radius: 12px; -fx-font-weight: bold; -fx-font-size: 11px;");
        }
    }

    private void simulateFileTamper() {
        File file = new File(evidence.getFilePath());
        if (!file.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File does not exist on disk to tamper.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try {
            if (originalFileBackup == null) {
                originalFileBackup = Files.readAllBytes(file.toPath());
            }
            // Append corrupted bytes to the file
            try (FileOutputStream fos = new FileOutputStream(file, true)) {
                fos.write("\n[TAMPERED_BYTE_PAYLOAD_FOR_DEMO]".getBytes());
            }
            executeIntegrityVerification();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not tamper file: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void restoreOriginalFile() {
        if (originalFileBackup == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No tamper state to restore (or file was not modified in this session).", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try {
            File file = new File(evidence.getFilePath());
            Files.write(file.toPath(), originalFileBackup);
            executeIntegrityVerification();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not restore original file: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * Builds Evidence Provenance Metadata Grid.
     */
    private VBox buildMetadataCard() {
        VBox metaCard = new VBox(16);
        metaCard.getStyleClass().add("soc-card");

        Label metaTitle = new Label("EVIDENCE PROVENANCE & IDENTIFICATION METADATA");
        metaTitle.getStyleClass().add("soc-card-title");

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(16);

        grid.add(createDetailItem("EVIDENCE ID", evidence.getEvidenceId()), 0, 0);
        grid.add(createDetailItem("CASE IDENTIFIER", evidence.getCaseId()), 1, 0);
        grid.add(createDetailItem("EVIDENCE TYPE", evidence.getEvidenceType()), 2, 0);

        grid.add(createDetailItem("COLLECTOR / INVESTIGATOR", evidence.getCollector()), 0, 1);
        grid.add(createDetailItem("SOURCE DEVICE / ORIGIN", evidence.getSourceDevice()), 1, 1);
        grid.add(createDetailItem("RECORDED AT (TIMESTAMP)", evidence.getRecordedAt()), 2, 1);

        grid.add(createDetailItem("FILE PATH", evidence.getFilePath()), 0, 2, 2, 1);
        grid.add(createDetailItem("FILE SIZE", formatBytes(evidence.getFileSize())), 2, 2);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            grid.getColumnConstraints().add(col);
        }

        metaCard.getChildren().addAll(metaTitle, grid);
        return metaCard;
    }

    /**
     * Builds Original SHA-256 Fingerprint card.
     */
    private VBox buildFingerprintCard() {
        VBox hashCard = new VBox(12);
        hashCard.getStyleClass().add("soc-card");

        HBox hashHeader = new HBox(12);
        hashHeader.setAlignment(Pos.CENTER_LEFT);

        Label hashTitle = new Label("ORIGINAL SHA-256 CRYPTOGRAPHIC FINGERPRINT");
        hashTitle.getStyleClass().add("soc-card-title");

        Region hashSpacer = new Region();
        HBox.setHgrow(hashSpacer, Priority.ALWAYS);

        Button copyBtn = new Button("📋 Copy Hash");
        copyBtn.getStyleClass().add("btn-secondary");
        copyBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(evidence.getSha256Hash());
            clipboard.setContent(content);
            copyBtn.setText("✓ Copied!");
        });

        hashHeader.getChildren().addAll(hashTitle, hashSpacer, copyBtn);

        VBox hashBox = new VBox(6);
        hashBox.getStyleClass().add("hash-display-box");

        Label hashVal = new Label(evidence.getSha256Hash());
        hashVal.getStyleClass().add("hash-text");
        hashVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #22d3ee; -fx-font-weight: bold;");

        Label hashNote = new Label("Anchored in SQLite and blockchain ledger block upon evidence registration.");
        hashNote.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        hashBox.getChildren().addAll(hashVal, hashNote);
        hashCard.getChildren().addAll(hashHeader, hashBox);
        return hashCard;
    }

    /**
     * Builds FEATURE 3: Chain of Custody History and Add Event Section.
     */
    private VBox buildCustodyHistoryCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("soc-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("FEATURE 3: CHAIN OF CUSTODY HISTORY");
        title.getStyleClass().add("soc-card-title");

        custodyCountLabel = new Label("Events: 0");
        custodyCountLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addEventBtn = new Button("⇄ Add Custody Event");
        addEventBtn.getStyleClass().add("btn-accent");
        addEventBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px;");
        addEventBtn.setOnAction(e -> openAddCustodyDialog());

        header.getChildren().addAll(title, custodyCountLabel, spacer, addEventBtn);

        // Table showing custody history chronologically
        custodyTable = new TableView<>();
        custodyTable.setPrefHeight(200);
        custodyTable.setPlaceholder(new Label("No custody events recorded for this evidence item."));

        TableColumn<CustodyEvent, String> timeCol = new TableColumn<>("TIMESTAMP");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp()));
        timeCol.setPrefWidth(150);

        TableColumn<CustodyEvent, String> actionCol = new TableColumn<>("ACTION");
        actionCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
        actionCol.setPrefWidth(140);
        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(action);
                    badge.setStyle(getBadgeStyleForAction(action));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<CustodyEvent, String> fromCol = new TableColumn<>("FROM / RELINQUISHED BY");
        fromCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActorFrom()));
        fromCol.setPrefWidth(170);

        TableColumn<CustodyEvent, String> toCol = new TableColumn<>("TO / RECEIVED BY");
        toCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActorTo()));
        toCol.setPrefWidth(170);

        TableColumn<CustodyEvent, String> notesCol = new TableColumn<>("NOTES / LOCATION");
        notesCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotes()));
        notesCol.setPrefWidth(260);

        custodyTable.getColumns().addAll(timeCol, actionCol, fromCol, toCol, notesCol);

        loadCustodyEvents();

        card.getChildren().addAll(header, custodyTable);
        return card;
    }

    private void loadCustodyEvents() {
        try {
            List<CustodyEvent> events = evidenceService.getCustodyEventsForEvidence(evidence.getEvidenceId());
            custodyTable.getItems().setAll(events);
            custodyCountLabel.setText("Events: " + events.size());
        } catch (SQLException e) {
            System.err.println("[EvidenceDetailView] Failed to load custody events: " + e.getMessage());
        }
    }

    private void openAddCustodyDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Record Chain of Custody Event");
        dialog.setHeaderText("Add Custody Transfer / State Transition for " + evidence.getEvidenceId());

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("soc-card");
        pane.setStyle("-fx-background-color: #0f172a; -fx-padding: 16px;");

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(14);
        form.setPadding(new Insets(10));

        // Action ComboBox
        ComboBox<String> actionCombo = new ComboBox<>();
        actionCombo.getItems().addAll("TRANSFERRED", "ANALYSIS STARTED", "RETURNED", "REGISTERED");
        actionCombo.getSelectionModel().select("TRANSFERRED");
        actionCombo.setMaxWidth(Double.MAX_VALUE);

        // From field (default to last recipient or collector)
        TextField fromField = new TextField(evidence.getCollector());
        fromField.setPromptText("Current handler name/badge");

        // To field
        TextField toField = new TextField();
        toField.setPromptText("Receiving analyst / facility");

        // Notes field
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Purpose of transfer, destination locker, or analysis scope...");
        notesArea.setPrefRowCount(3);

        form.add(createFormLabel("ACTION *"), 0, 0);
        form.add(actionCombo, 1, 0);

        form.add(createFormLabel("FROM (RELINQUISHING USER) *"), 0, 1);
        form.add(fromField, 1, 1);

        form.add(createFormLabel("TO (RECEIVING USER) *"), 0, 2);
        form.add(toField, 1, 2);

        form.add(createFormLabel("NOTES / FACILITY LOCATION"), 0, 3);
        form.add(notesArea, 1, 3);

        pane.setContent(form);

        ButtonType submitType = new ButtonType("Commit Custody Event", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        Button submitBtn = (Button) pane.lookupButton(submitType);
        submitBtn.getStyleClass().add("btn-primary");

        dialog.setResultConverter(btn -> {
            if (btn == submitType) {
                String action = actionCombo.getValue();
                String from = fromField.getText();
                String to = toField.getText();
                String notes = notesArea.getText();

                if (from == null || from.trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "From / Relinquishing user is required.", ButtonType.OK);
                    alert.showAndWait();
                    return false;
                }

                try {
                    evidenceService.addCustodyEvent(evidence.getEvidenceId(), action, from, to, notes);
                    return true;
                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to persist custody event: " + ex.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadCustodyEvents();
            }
        });
    }

    private String getBadgeStyleForAction(String action) {
        return switch (action) {
            case "REGISTERED" -> "-fx-background-color: rgba(6, 182, 212, 0.2); -fx-text-fill: #22d3ee; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "TRANSFERRED" -> "-fx-background-color: rgba(59, 130, 246, 0.2); -fx-text-fill: #60a5fa; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "ANALYSIS STARTED" -> "-fx-background-color: rgba(245, 158, 11, 0.2); -fx-text-fill: #fbbf24; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "RETURNED" -> "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #34d399; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            default -> "-fx-background-color: rgba(148, 163, 184, 0.2); -fx-text-fill: #cbd5e1; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;";
        };
    }

    private Label createFormLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        return lbl;
    }

    private VBox createDetailItem(String label, String value) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("detail-label");

        Label val = new Label(value != null ? value : "-");
        val.getStyleClass().add("detail-value");
        val.setWrapText(true);

        box.getChildren().addAll(lbl, val);
        return box;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB (%d bytes)", bytes / Math.pow(1024, exp), pre, bytes);
    }
}
