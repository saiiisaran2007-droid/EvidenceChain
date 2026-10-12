package com.evidencechain.controller;

import com.evidencechain.model.Evidence;
import com.evidencechain.service.EvidenceService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * View and controller component for Feature 1: Evidence Registration.
 */
public class RegisterEvidenceView {

    private final EvidenceService evidenceService;
    private final Consumer<Evidence> onEvidenceRegistered;
    private final Runnable onViewCatalog;

    private TextField caseIdField;
    private ComboBox<String> evidenceTypeCombo;
    private TextField collectorField;
    private TextField sourceDeviceField;
    private TextArea descriptionArea;

    private Label selectedFileLabel;
    private Label selectedFileSizeLabel;
    private Label hashDisplayLabel;
    private VBox alertBox;
    private Label alertMessageLabel;

    private File currentSelectedFile;

    public RegisterEvidenceView(EvidenceService evidenceService,
                                Consumer<Evidence> onEvidenceRegistered,
                                Runnable onViewCatalog) {
        this.evidenceService = evidenceService;
        this.onEvidenceRegistered = onEvidenceRegistered;
        this.onViewCatalog = onViewCatalog;
    }

    public Node createView() {
        VBox root = new VBox(20);

        // Feedback alert box (hidden by default)
        alertBox = new VBox(6);
        alertBox.getStyleClass().addAll("alert-box", "alert-success");
        alertBox.setVisible(false);
        alertBox.setManaged(false);

        alertMessageLabel = new Label();
        alertBox.getChildren().add(alertMessageLabel);

        // Form Card
        VBox formCard = new VBox(18);
        formCard.getStyleClass().add("soc-card");

        Label cardHeader = new Label("DIGITAL EVIDENCE INTAKE FORM");
        cardHeader.getStyleClass().add("soc-card-title");
        formCard.getChildren().add(cardHeader);

        // Grid for form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(14);

        // Column 1: Case ID
        VBox caseIdBox = createFormField("CASE IDENTIFIER *", caseIdField = new TextField());
        caseIdField.setPromptText("e.g., CASE-2026-0042");

        // Column 2: Evidence Type
        evidenceTypeCombo = new ComboBox<>();
        evidenceTypeCombo.getItems().addAll(
                "Disk Image (.img / .dd / .e01)",
                "Memory Dump (.raw / .dmp)",
                "Network Capture (.pcap / .pcapng)",
                "System / Security Log (.log / .evtx)",
                "Mobile Extraction (.tar / .zip)",
                "Document / Email (.pdf / .eml)",
                "Encrypted Volume (.bin)",
                "Other Forensic Artifact"
        );
        evidenceTypeCombo.getSelectionModel().selectFirst();
        evidenceTypeCombo.setMaxWidth(Double.MAX_VALUE);
        VBox typeBox = createFormField("EVIDENCE TYPE *", evidenceTypeCombo);

        formGrid.add(caseIdBox, 0, 0);
        formGrid.add(typeBox, 1, 0);

        // Row 2: Collector and Source Device
        VBox collectorBox = createFormField("COLLECTOR / INVESTIGATOR *", collectorField = new TextField());
        collectorField.setPromptText("e.g., Investigator J. Doe, Badge #412");

        VBox deviceBox = createFormField("SOURCE DEVICE / ORIGIN *", sourceDeviceField = new TextField());
        sourceDeviceField.setPromptText("e.g., Dell XPS 15 (SN: 8847-BC) seized from suspect office");

        formGrid.add(collectorBox, 0, 1);
        formGrid.add(deviceBox, 1, 1);

        // Make columns 50% / 50%
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        formGrid.getColumnConstraints().addAll(col1, col2);

        formCard.getChildren().add(formGrid);

        // Description
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the nature of the evidence, condition at seizure, custody notes, or preliminary observations...");
        descriptionArea.setPrefRowCount(3);
        VBox descBox = createFormField("FORENSIC DESCRIPTION & NOTES", descriptionArea);
        formCard.getChildren().add(descBox);

        // File Selector & Hash Section
        VBox fileSection = buildFileAndHashSection();
        formCard.getChildren().add(fileSection);

        // Action Buttons Row
        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Button clearBtn = new Button("Clear Form");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> clearForm());

        Button submitBtn = new Button("⊕ Register Evidence & Commit Hash");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setOnAction(e -> handleSubmit());

        buttonRow.getChildren().addAll(clearBtn, submitBtn);
        formCard.getChildren().add(buttonRow);

        root.getChildren().addAll(alertBox, formCard);
        return root;
    }

    private VBox buildFileAndHashSection() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #0b1120; -fx-padding: 16px; -fx-border-color: #1e293b; -fx-border-radius: 6px; -fx-background-radius: 6px;");

        Label sectionTitle = new Label("CRYPTOGRAPHIC FINGERPRINTING (SHA-256)");
        sectionTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");

        HBox filePickerRow = new HBox(14);
        filePickerRow.setAlignment(Pos.CENTER_LEFT);

        Button browseBtn = new Button("📁 Select Local File...");
        browseBtn.getStyleClass().add("btn-secondary");
        browseBtn.setOnAction(e -> handleSelectFile());

        VBox fileMetaBox = new VBox(2);
        selectedFileLabel = new Label("No file selected.");
        selectedFileLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 13px; -fx-font-weight: bold;");

        selectedFileSizeLabel = new Label("Select a file to compute cryptographic SHA-256 hash.");
        selectedFileSizeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        fileMetaBox.getChildren().addAll(selectedFileLabel, selectedFileSizeLabel);

        filePickerRow.getChildren().addAll(browseBtn, fileMetaBox);

        // Hash Display
        VBox hashContainer = new VBox(6);
        hashContainer.getStyleClass().add("hash-display-box");

        Label hashTitle = new Label("CALCULATED SHA-256 CHECKSUM");
        hashTitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold;");

        hashDisplayLabel = new Label("e3b0c442... (Will be calculated immediately upon file selection)");
        hashDisplayLabel.getStyleClass().add("hash-text");
        hashDisplayLabel.setWrapText(true);

        hashContainer.getChildren().addAll(hashTitle, hashDisplayLabel);

        box.getChildren().addAll(sectionTitle, filePickerRow, hashContainer);
        return box;
    }

    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Digital Evidence File");
        File file = fileChooser.showOpenDialog(null);

        if (file == null) {
            // User cancelled file selection - preserve previous selection if any
            return;
        }

        if (!file.exists() || !file.isFile()) {
            showError("The selected file does not exist or is not a regular file.");
            return;
        }

        this.currentSelectedFile = file;
        this.selectedFileLabel.setText(file.getName() + " (" + file.getAbsolutePath() + ")");
        this.selectedFileSizeLabel.setText("File Size: " + formatBytes(file.length()));

        // Calculate real SHA-256 hash using MessageDigest
        try {
            String sha256 = evidenceService.previewFileHash(file);
            hashDisplayLabel.setText(sha256);
            hashDisplayLabel.setStyle("-fx-text-fill: #22d3ee; -fx-font-family: 'Consolas', monospace; -fx-font-weight: bold;");
            hideAlert();
        } catch (IOException ex) {
            hashDisplayLabel.setText("Error reading file: " + ex.getMessage());
            hashDisplayLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-family: 'Consolas', monospace;");
            showError("Failed to calculate SHA-256 hash: " + ex.getMessage());
        }
    }

    private void handleSubmit() {
        String caseId = caseIdField.getText();
        String evidenceType = evidenceTypeCombo.getValue();
        String collector = collectorField.getText();
        String sourceDevice = sourceDeviceField.getText();
        String description = descriptionArea.getText();

        if (caseId == null || caseId.trim().isEmpty()) {
            showError("Validation Error: Please enter a Case ID.");
            caseIdField.requestFocus();
            return;
        }

        if (collector == null || collector.trim().isEmpty()) {
            showError("Validation Error: Please enter the Collector / Investigator name.");
            collectorField.requestFocus();
            return;
        }

        if (sourceDevice == null || sourceDevice.trim().isEmpty()) {
            showError("Validation Error: Please specify the Source Device / Origin.");
            sourceDeviceField.requestFocus();
            return;
        }

        if (currentSelectedFile == null || !currentSelectedFile.exists()) {
            showError("Validation Error: Please select an existing file on disk.");
            return;
        }

        try {
            Evidence registered = evidenceService.registerEvidence(
                    caseId,
                    evidenceType,
                    description,
                    collector,
                    sourceDevice,
                    currentSelectedFile
            );

            // Display success alert
            showSuccess(registered);

            // Notify parent to refresh counts / dashboard
            if (onEvidenceRegistered != null) {
                onEvidenceRegistered.accept(registered);
            }

            // Clear inputs for next registration
            clearInputFields();

        } catch (IllegalArgumentException ex) {
            showError("Input Error: " + ex.getMessage());
        } catch (IOException ex) {
            showError("File I/O Error: Unable to read file for cryptographic fingerprinting (" + ex.getMessage() + ")");
        } catch (SQLException ex) {
            showError("Database Error: Failed to store evidence record in SQLite (" + ex.getMessage() + ")");
        } catch (Exception ex) {
            showError("Unexpected error occurred: " + ex.getMessage());
        }
    }

    private void showSuccess(Evidence e) {
        alertBox.getStyleClass().setAll("alert-box", "alert-success");

        VBox content = new VBox(6);
        Label heading = new Label("✓ EVIDENCE SUCCESSFULLY REGISTERED");
        heading.getStyleClass().add("alert-success-text");

        Label detail = new Label("Evidence ID: " + e.getEvidenceId() + " | Case: " + e.getCaseId() + " | Recorded: " + e.getRecordedAt());
        detail.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");

        Label hashLbl = new Label("SHA-256 Fingerprint: " + e.getSha256Hash());
        hashLbl.setStyle("-fx-text-fill: #34d399; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");

        HBox btnRow = new HBox(10);
        btnRow.setPadding(new Insets(4, 0, 0, 0));

        Button viewCatalogBtn = new Button("View in Evidence Catalog →");
        viewCatalogBtn.getStyleClass().add("btn-accent");
        viewCatalogBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 12px;");
        viewCatalogBtn.setOnAction(evt -> {
            if (onViewCatalog != null) {
                onViewCatalog.run();
            }
        });

        btnRow.getChildren().add(viewCatalogBtn);
        content.getChildren().addAll(heading, detail, hashLbl, btnRow);

        alertBox.getChildren().setAll(content);
        alertBox.setVisible(true);
        alertBox.setManaged(true);
    }

    private void showError(String message) {
        alertBox.getStyleClass().setAll("alert-box", "alert-error");
        alertMessageLabel.setText("⚠ " + message);
        alertMessageLabel.getStyleClass().setAll("alert-error-text");
        alertBox.getChildren().setAll(alertMessageLabel);
        alertBox.setVisible(true);
        alertBox.setManaged(true);
    }

    private void hideAlert() {
        alertBox.setVisible(false);
        alertBox.setManaged(false);
    }

    private void clearInputFields() {
        caseIdField.clear();
        collectorField.clear();
        sourceDeviceField.clear();
        descriptionArea.clear();
        currentSelectedFile = null;
        selectedFileLabel.setText("No file selected.");
        selectedFileSizeLabel.setText("Select a file to compute cryptographic SHA-256 hash.");
        hashDisplayLabel.setText("e3b0c442... (Will be calculated immediately upon file selection)");
        hashDisplayLabel.setStyle("-fx-text-fill: #22d3ee; -fx-font-family: 'Consolas', monospace;");
    }

    private void clearForm() {
        clearInputFields();
        hideAlert();
    }

    private VBox createFormField(String labelText, Control inputControl) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        box.getChildren().addAll(label, inputControl);
        return box;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB (%d bytes)", bytes / Math.pow(1024, exp), pre, bytes);
    }
}
