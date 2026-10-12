package com.evidencechain.controller;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.LedgerBlock;
import com.evidencechain.model.LedgerVerificationResult;
import com.evidencechain.service.LedgerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * View component for Feature 4: Blockchain-Style Hash-Linked Ledger.
 * Displays block sequence, shortened hashes, detailed block inspector,
 * and recalculation-based ledger integrity verification.
 */
public class LedgerView {

    private final LedgerService ledgerService;
    private final ObservableList<LedgerBlock> blockList = FXCollections.observableArrayList();

    private TableView<LedgerBlock> ledgerTable;
    private VBox verificationBanner;
    private Label verificationStatusLabel;
    private Label verificationDetailLabel;
    private Label totalBlocksLabel;
    private VBox blockDetailCard;

    private Long tamperedBlockIndex = null;
    private String originalTamperedAction = null;

    public LedgerView(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    public Node createView() {
        VBox root = new VBox(18);

        // Header and Controls Row
        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("BLOCKCHAIN-STYLE HASH-LINKED LEDGER");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Immutable, cryptographically chained audit blocks linking all forensic custody transitions.");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("↻ Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadBlocks());

        Button verifyBtn = new Button("🛡 Verify Ledger");
        verifyBtn.getStyleClass().add("btn-primary");
        verifyBtn.setStyle("-fx-font-size: 13px; -fx-padding: 8px 18px;");
        verifyBtn.setOnAction(e -> executeLedgerVerification());

        topRow.getChildren().addAll(titleBox, spacer, refreshBtn, verifyBtn);

        // Verification Status Banner (prominent)
        verificationBanner = new VBox(6);
        verificationBanner.setStyle("-fx-background-color: #131d33; -fx-padding: 14px 18px; -fx-border-color: #1e293b; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        verificationStatusLabel = new Label("LEDGER STATUS: READY TO AUDIT");
        verificationStatusLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 14px; -fx-font-weight: bold;");

        verificationDetailLabel = new Label("Click 'Verify Ledger' to recalculate SHA-256 block hashes and validate previousHash pointers across all blocks.");
        verificationDetailLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        verificationBanner.getChildren().addAll(verificationStatusLabel, verificationDetailLabel);

        // KPI Summary Row
        HBox kpiRow = new HBox(14);
        totalBlocksLabel = new Label("0 Blocks");
        totalBlocksLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox kpi1 = createMiniKpi("CHAIN LENGTH", totalBlocksLabel);
        VBox kpi2 = createMiniKpi("HASH ALGORITHM", new Label("SHA-256"));
        VBox kpi3 = createMiniKpi("GENESIS ROOT", new Label("Block #0 Anchored"));
        kpiRow.getChildren().addAll(kpi1, kpi2, kpi3);

        // Ledger Table
        ledgerTable = new TableView<>();
        ledgerTable.setPrefHeight(260);
        VBox.setVgrow(ledgerTable, Priority.ALWAYS);
        ledgerTable.setPlaceholder(new Label("No blocks found in ledger. Genesis block will be generated automatically."));

        // Column 1: Block Number
        TableColumn<LedgerBlock, String> indexCol = new TableColumn<>("BLOCK #");
        indexCol.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getBlockIndex()));
        indexCol.setPrefWidth(90);

        // Column 2: Evidence ID
        TableColumn<LedgerBlock, String> evidenceIdCol = new TableColumn<>("EVIDENCE ID");
        evidenceIdCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEvidenceId()));
        evidenceIdCol.setPrefWidth(120);

        // Column 3: Action
        TableColumn<LedgerBlock, String> actionCol = new TableColumn<>("ACTION");
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
                    badge.setStyle(getActionStyle(action));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Column 4: Timestamp
        TableColumn<LedgerBlock, String> timeCol = new TableColumn<>("TIMESTAMP");
        timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp()));
        timeCol.setPrefWidth(150);

        // Column 5: Shortened Current Hash
        TableColumn<LedgerBlock, String> hashCol = new TableColumn<>("CURRENT HASH (SHA-256)");
        hashCol.setCellValueFactory(d -> {
            String full = d.getValue().getBlockHash();
            return new SimpleStringProperty(shortenHash(full));
        });
        hashCol.setPrefWidth(210);

        // Column 6: Shortened Previous Hash
        TableColumn<LedgerBlock, String> prevHashCol = new TableColumn<>("PREVIOUS HASH");
        prevHashCol.setCellValueFactory(d -> {
            String full = d.getValue().getPreviousHash();
            return new SimpleStringProperty(shortenHash(full));
        });
        prevHashCol.setPrefWidth(210);

        ledgerTable.getColumns().addAll(indexCol, evidenceIdCol, actionCol, timeCol, hashCol, prevHashCol);

        // Block Detail Card (selected row)
        blockDetailCard = new VBox(10);
        blockDetailCard.getStyleClass().add("soc-card");
        Label detailTitle = new Label("BLOCK CRYPTOGRAPHIC INSPECTOR (Select a block row above)");
        detailTitle.getStyleClass().add("soc-card-title");
        blockDetailCard.getChildren().add(detailTitle);

        ledgerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                renderBlockDetail(newVal);
            }
        });

        // Tamper Demo controls for Ledger presentation
        HBox demoRow = new HBox(12);
        demoRow.setAlignment(Pos.CENTER_LEFT);
        Label demoLabel = new Label("LEDGER AUDIT TESTING TOOLS:");
        demoLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");

        Button tamperBlockBtn = new Button("Simulate Database Tamper (Corrupt Ledger Block)");
        tamperBlockBtn.getStyleClass().add("btn-secondary");
        tamperBlockBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #f87171; -fx-border-color: #7f1d1d;");
        tamperBlockBtn.setOnAction(e -> simulateLedgerTampering());

        Button restoreBlockBtn = new Button("Restore Tampered Block");
        restoreBlockBtn.getStyleClass().add("btn-secondary");
        restoreBlockBtn.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399; -fx-border-color: #064e3b;");
        restoreBlockBtn.setOnAction(e -> restoreLedgerBlock());

        demoRow.getChildren().addAll(demoLabel, tamperBlockBtn, restoreBlockBtn);

        root.getChildren().addAll(topRow, verificationBanner, kpiRow, ledgerTable, blockDetailCard, demoRow);

        loadBlocks();
        return root;
    }

    public void loadBlocks() {
        try {
            List<LedgerBlock> blocks = ledgerService.getAllBlocks();
            blockList.setAll(blocks);
            ledgerTable.setItems(blockList);
            totalBlocksLabel.setText(blocks.size() + " Blocks");

            if (!blocks.isEmpty() && ledgerTable.getSelectionModel().isEmpty()) {
                ledgerTable.getSelectionModel().selectLast();
            }
        } catch (SQLException e) {
            System.err.println("[LedgerView] Error loading blocks: " + e.getMessage());
        }
    }

    public void executeLedgerVerification() {
        LedgerVerificationResult result = ledgerService.verifyLedger();
        verificationBanner.getChildren().clear();

        if (result.isValid()) {
            // GREEN "LEDGER VALID"
            verificationBanner.setStyle("-fx-background-color: rgba(5, 150, 105, 0.15); -fx-padding: 14px 18px; -fx-border-color: #059669; -fx-border-radius: 8px; -fx-background-radius: 8px;");

            Label statusLbl = new Label("LEDGER VALID");
            statusLbl.setStyle("-fx-background-color: #059669; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 6px 14px; -fx-background-radius: 4px;");

            Label detailLbl = new Label("All " + result.getTotalBlocks() + " blocks cryptographically verified. SHA-256 block hashes and previous-hash pointers are strictly consistent.");
            detailLbl.setStyle("-fx-text-fill: #34d399; -fx-font-size: 13px; -fx-font-weight: bold;");

            verificationBanner.getChildren().addAll(statusLbl, detailLbl);
        } else {
            // RED "LEDGER COMPROMISED"
            verificationBanner.setStyle("-fx-background-color: rgba(220, 38, 38, 0.15); -fx-padding: 14px 18px; -fx-border-color: #dc2626; -fx-border-radius: 8px; -fx-background-radius: 8px;");

            Label statusLbl = new Label("LEDGER COMPROMISED");
            statusLbl.setStyle("-fx-background-color: #dc2626; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 6px 14px; -fx-background-radius: 4px;");

            Label detailLbl = new Label(result.getStatusMessage());
            detailLbl.setStyle("-fx-text-fill: #f87171; -fx-font-size: 13px; -fx-font-weight: bold;");

            Label note = new Label("Chain of custody continuity broken at Block #" + result.getFailedBlockIndex() + "! Possible unauthorized tampering in SQLite database.");
            note.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");

            verificationBanner.getChildren().addAll(statusLbl, detailLbl, note);
        }
    }

    private void renderBlockDetail(LedgerBlock block) {
        blockDetailCard.getChildren().clear();

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("BLOCK #" + block.getBlockIndex() + " CRYPTOGRAPHIC INSPECTOR");
        title.getStyleClass().add("soc-card-title");

        Label actionBadge = new Label(block.getAction());
        actionBadge.setStyle(getActionStyle(block.getAction()));

        header.getChildren().addAll(title, actionBadge);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(8);

        grid.add(createHashField("TARGET EVIDENCE ID", block.getEvidenceId()), 0, 0);
        grid.add(createHashField("RECORDED TIMESTAMP", block.getTimestamp()), 1, 0);

        grid.add(createHashField("DATA PAYLOAD HASH", block.getDataHash()), 0, 1, 2, 1);
        grid.add(createHashField("PREVIOUS BLOCK HASH", block.getPreviousHash()), 0, 2, 2, 1);
        grid.add(createHashField("CURRENT BLOCK HASH (SHA-256)", block.getBlockHash()), 0, 3, 2, 1);

        blockDetailCard.getChildren().addAll(header, grid);
    }

    private VBox createHashField(String label, String value) {
        VBox box = new VBox(2);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold;");

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #22d3ee; -fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");
        val.setWrapText(true);

        box.getChildren().addAll(lbl, val);
        return box;
    }

    private VBox createMiniKpi(String title, Label valueLabel) {
        VBox box = new VBox(4);
        box.getStyleClass().add("soc-card");
        box.setPadding(new Insets(10, 16, 10, 16));
        box.setPrefWidth(220);

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold;");

        box.getChildren().addAll(t, valueLabel);
        return box;
    }

    private String shortenHash(String hash) {
        if (hash == null) return "-";
        if (hash.length() > 20) {
            return hash.substring(0, 10) + "..." + hash.substring(hash.length() - 8);
        }
        return hash;
    }

    private String getActionStyle(String action) {
        return switch (action) {
            case "GENESIS" -> "-fx-background-color: rgba(139, 92, 246, 0.2); -fx-text-fill: #a78bfa; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "REGISTERED" -> "-fx-background-color: rgba(6, 182, 212, 0.2); -fx-text-fill: #22d3ee; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "TRANSFERRED" -> "-fx-background-color: rgba(59, 130, 246, 0.2); -fx-text-fill: #60a5fa; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "ANALYSIS STARTED" -> "-fx-background-color: rgba(245, 158, 11, 0.2); -fx-text-fill: #fbbf24; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            case "RETURNED" -> "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #34d399; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11px;";
            default -> "-fx-background-color: rgba(148, 163, 184, 0.2); -fx-text-fill: #cbd5e1; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;";
        };
    }

    private void simulateLedgerTampering() {
        if (blockList.isEmpty()) {
            return;
        }

        // Tamper with the latest block or block #1
        LedgerBlock target = blockList.get(blockList.size() - 1);
        tamperedBlockIndex = target.getBlockIndex();
        originalTamperedAction = target.getAction();

        String sql = "UPDATE ledger_blocks SET action = 'UNAUTHORIZED_ALTERATION' WHERE block_index = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tamperedBlockIndex);
            ps.executeUpdate();

            loadBlocks();
            executeLedgerVerification();
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to tamper block: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void restoreLedgerBlock() {
        if (tamperedBlockIndex == null || originalTamperedAction == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No tampered block recorded to restore.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String sql = "UPDATE ledger_blocks SET action = ? WHERE block_index = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, originalTamperedAction);
            ps.setLong(2, tamperedBlockIndex);
            ps.executeUpdate();

            tamperedBlockIndex = null;
            originalTamperedAction = null;

            loadBlocks();
            executeLedgerVerification();
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to restore block: " + ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }
}
