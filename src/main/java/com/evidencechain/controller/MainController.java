package com.evidencechain.controller;

import com.evidencechain.database.DatabaseManager;
import com.evidencechain.model.CustodyEvent;
import com.evidencechain.model.Evidence;
import com.evidencechain.model.LedgerVerificationResult;
import com.evidencechain.service.EvidenceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the primary EvidenceChain application shell and sidebar navigation.
 * Manages view routing between Dashboard, Evidence Catalog, Registration, Details,
 * Chain of Custody, Blockchain-Style Ledger, and Cryptographic Integrity Verification.
 */
public class MainController {

    private final BorderPane rootLayout;
    private final VBox contentArea;
    private final Label pageTitleLabel;
    private final Label pageSubtitleLabel;
    private final Label statusDbLabel;
    private final Label statusLedgerLabel;

    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private String activeScreen = "Dashboard";

    private final EvidenceService evidenceService;
    private Evidence selectedEvidenceForDetails;

    public MainController() {
        this.evidenceService = new EvidenceService();

        this.rootLayout = new BorderPane();
        this.contentArea = new VBox(20);
        this.contentArea.getStyleClass().add("content-area");

        this.pageTitleLabel = new Label("Security Operations Dashboard");
        this.pageTitleLabel.getStyleClass().add("page-title");

        this.pageSubtitleLabel = new Label("Real-time digital forensics evidence custody and blockchain ledger overview");
        this.pageSubtitleLabel.getStyleClass().add("page-subtitle");

        this.statusDbLabel = new Label("● DB: CONNECTED");
        this.statusDbLabel.getStyleClass().add("system-status-pill");

        this.statusLedgerLabel = new Label("● LEDGER: ACTIVE");
        this.statusLedgerLabel.getStyleClass().add("badge-info");

        initializeLayout();
        showScreen("Dashboard");
    }

    public BorderPane getRootLayout() {
        return rootLayout;
    }

    private void initializeLayout() {
        // 1. Sidebar Navigation
        VBox sidebar = buildSidebar();
        rootLayout.setLeft(sidebar);

        // 2. Top Header Bar
        HBox topHeader = buildTopHeader();

        // 3. Central Content wrapper
        VBox centerContainer = new VBox();
        centerContainer.getChildren().add(topHeader);

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0b0f19; -fx-background-color: #0b0f19; -fx-border-width: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        centerContainer.getChildren().add(scrollPane);
        rootLayout.setCenter(centerContainer);

        // 4. Bottom Status Bar
        HBox statusBar = buildStatusBar();
        rootLayout.setBottom(statusBar);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Branding
        VBox brandBox = new VBox(6);
        brandBox.getStyleClass().add("brand-container");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label brandTitle = new Label("EVIDENCECHAIN");
        brandTitle.getStyleClass().add("brand-title");

        Label badge = new Label("MVP");
        badge.getStyleClass().add("brand-badge");
        titleRow.getChildren().addAll(brandTitle, badge);

        Label brandSub = new Label("Forensics Custody & Ledger");
        brandSub.getStyleClass().add("brand-subtitle");
        brandBox.getChildren().addAll(titleRow, brandSub);

        // Navigation Sections
        Label coreSection = new Label("CORE MODULES");
        coreSection.getStyleClass().add("nav-section-label");

        sidebar.getChildren().addAll(brandBox, coreSection);

        String[] navItems = {
            "Dashboard",
            "Evidence",
            "Register Evidence",
            "Evidence Details",
            "Chain of Custody",
            "Blockchain Ledger",
            "Integrity Verification"
        };

        for (String item : navItems) {
            Button btn = new Button(getIconForNav(item) + "  " + item);
            btn.getStyleClass().add("nav-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> showScreen(item));
            navButtons.put(item, btn);
            sidebar.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // System Info card at bottom of sidebar
        VBox infoCard = new VBox(4);
        infoCard.setPadding(new Insets(16));
        infoCard.setStyle("-fx-border-color: #1e293b; -fx-border-width: 1px 0 0 0;");
        Label envLabel = new Label("ENVIRONMENT: LOCAL SOC");
        envLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label hashAlg = new Label("HASH ENGINE: SHA-256");
        hashAlg.setStyle("-fx-text-fill: #06b6d4; -fx-font-size: 11px;");
        infoCard.getChildren().addAll(envLabel, hashAlg);
        sidebar.getChildren().add(infoCard);

        return sidebar;
    }

    private String getIconForNav(String name) {
        return switch (name) {
            case "Dashboard" -> "⊞";
            case "Evidence" -> "🗎";
            case "Register Evidence" -> "⊕";
            case "Evidence Details" -> "🔍";
            case "Chain of Custody" -> "⇄";
            case "Blockchain Ledger" -> "⛓";
            case "Integrity Verification" -> "✓";
            default -> "•";
        };
    }

    private HBox buildTopHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("top-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        titleBox.getChildren().addAll(pageTitleLabel, pageSubtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        updateHeaderStatus();

        header.getChildren().addAll(titleBox, spacer, statusDbLabel, statusLedgerLabel);
        return header;
    }

    private void updateHeaderStatus() {
        boolean dbOk = DatabaseManager.isConnected();
        if (dbOk) {
            statusDbLabel.setText("● SQLITE CONNECTED");
            statusDbLabel.getStyleClass().setAll("system-status-pill");
        } else {
            statusDbLabel.setText("● SQLITE OFFLINE");
            statusDbLabel.setStyle("-fx-text-fill: #ef4444; -fx-background-color: rgba(239,68,68,0.1);");
        }

        try {
            LedgerVerificationResult ledgerRes = evidenceService.getLedgerService().verifyLedger();
            if (ledgerRes.isValid()) {
                statusLedgerLabel.setText("● LEDGER: VALID (" + ledgerRes.getTotalBlocks() + " BLOCKS)");
                statusLedgerLabel.setStyle("-fx-background-color: rgba(16, 185, 129, 0.15); -fx-text-fill: #34d399; -fx-font-size: 11px; -fx-padding: 4px 12px; -fx-background-radius: 12px; -fx-font-weight: bold;");
            } else {
                statusLedgerLabel.setText("⚠ LEDGER: COMPROMISED");
                statusLedgerLabel.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #f87171; -fx-font-size: 11px; -fx-padding: 4px 12px; -fx-background-radius: 12px; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            statusLedgerLabel.setText("● LEDGER: ACTIVE");
        }
    }

    private HBox buildStatusBar() {
        HBox bar = new HBox(20);
        bar.getStyleClass().add("status-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label dbPath = new Label("Database: " + DatabaseManager.getDatabaseLocation());
        dbPath.getStyleClass().add("status-bar-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Label timeLabel = new Label("Session Active: " + now);
        timeLabel.getStyleClass().add("status-bar-text");

        bar.getChildren().addAll(dbPath, spacer, timeLabel);
        return bar;
    }

    public void showScreen(String screenName) {
        this.activeScreen = screenName;
        updateHeaderStatus();

        // Update nav active button style
        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(screenName)) {
                if (!entry.getValue().getStyleClass().contains("nav-button-active")) {
                    entry.getValue().getStyleClass().add("nav-button-active");
                }
            } else {
                entry.getValue().getStyleClass().remove("nav-button-active");
            }
        }

        contentArea.getChildren().clear();

        switch (screenName) {
            case "Dashboard" -> renderDashboard();
            case "Evidence" -> renderEvidenceList();
            case "Register Evidence" -> renderRegisterEvidence();
            case "Evidence Details" -> renderEvidenceDetails();
            case "Chain of Custody" -> renderCustodyView();
            case "Blockchain Ledger" -> renderLedgerView();
            case "Integrity Verification" -> renderIntegrityVerificationView();
            default -> renderDashboard();
        }
    }

    public void openEvidenceDetails(Evidence evidence) {
        this.selectedEvidenceForDetails = evidence;
        showScreen("Evidence Details");
    }

    private void renderDashboard() {
        pageTitleLabel.setText("Security Operations Dashboard");
        pageSubtitleLabel.setText("Real-time digital forensics evidence custody and blockchain ledger overview");

        int totalEvidence = 0;
        int totalCases = 0;
        int verifiedCount = 0;
        List<Evidence> recentEvidence = List.of();
        List<CustodyEvent> recentCustody = List.of();
        LedgerVerificationResult ledgerRes;

        try {
            totalEvidence = evidenceService.getTotalEvidenceCount();
            totalCases = evidenceService.getTotalCasesCount();
            verifiedCount = evidenceService.getVerifiedEvidenceCount();
            recentEvidence = evidenceService.getRecentEvidence(5);
            recentCustody = evidenceService.getRecentCustodyEvents(5);
        } catch (SQLException e) {
            System.err.println("[MainController] Database query error on dashboard: " + e.getMessage());
        }

        ledgerRes = evidenceService.getLedgerService().verifyLedger();

        // 4 KPI Summary Cards (Real database values)
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        grid.add(createKpiCard("TOTAL EVIDENCE", String.valueOf(totalEvidence), "Tracked digital artifacts", "#06b6d4"), 0, 0);
        grid.add(createKpiCard("TOTAL CASES", String.valueOf(totalCases), "Under active investigation", "#3b82f6"), 1, 0);
        grid.add(createKpiCard("VERIFIED EVIDENCE", verifiedCount + " / " + totalEvidence, "Physical disk files intact", "#10b981"), 2, 0);

        String ledgerStatusStr = ledgerRes.isValid() ? "LEDGER VALID" : "COMPROMISED";
        String ledgerMetaStr = ledgerRes.getTotalBlocks() + " blocks cryptographically linked";
        String ledgerColor = ledgerRes.isValid() ? "#10b981" : "#ef4444";
        grid.add(createKpiCard("LEDGER STATUS", ledgerStatusStr, ledgerMetaStr, ledgerColor), 3, 0);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        // Action Banner
        HBox banner = new HBox(16);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setStyle("-fx-background-color: #131d33; -fx-padding: 16px 20px; -fx-border-color: #1e293b; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        VBox bannerText = new VBox(4);
        Label bannerTitle = new Label("FORENSIC INGESTION & LEDGER READY");
        bannerTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label bannerSub = new Label("Ingest physical media, compute SHA-256 fingerprint, commit hash-linked blocks, and track custody.");
        bannerSub.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        bannerText.getChildren().addAll(bannerTitle, bannerSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button regBtn = new Button("⊕ Register New Evidence");
        regBtn.getStyleClass().add("btn-primary");
        regBtn.setOnAction(e -> showScreen("Register Evidence"));

        banner.getChildren().addAll(bannerText, spacer, regBtn);

        // Recent Evidence Table Card
        VBox recentCard = buildRecentEvidenceCard(recentEvidence);

        // Recent Custody Events Table Card
        VBox custodyCard = buildRecentCustodyCard(recentCustody);

        contentArea.getChildren().addAll(grid, banner, recentCard, custodyCard);
    }

    private VBox buildRecentEvidenceCard(List<Evidence> recentEvidence) {
        VBox recentCard = new VBox(14);
        recentCard.getStyleClass().add("soc-card");

        HBox recentHeader = new HBox(12);
        recentHeader.setAlignment(Pos.CENTER_LEFT);
        Label recentTitle = new Label("RECENT REGISTERED EVIDENCE (REAL SQLITE RECORDS)");
        recentTitle.getStyleClass().add("soc-card-title");

        Region rSpacer = new Region();
        HBox.setHgrow(rSpacer, Priority.ALWAYS);

        Button viewAllBtn = new Button("View Full Catalog →");
        viewAllBtn.getStyleClass().add("btn-secondary");
        viewAllBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
        viewAllBtn.setOnAction(e -> showScreen("Evidence"));

        recentHeader.getChildren().addAll(recentTitle, rSpacer, viewAllBtn);

        if (recentEvidence.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(24));
            Label emptyLbl = new Label("No evidence items registered yet in the database.");
            emptyLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
            Button emptyReg = new Button("Register First Evidence Item");
            emptyReg.getStyleClass().add("btn-accent");
            emptyReg.setOnAction(e -> showScreen("Register Evidence"));
            emptyBox.getChildren().addAll(emptyLbl, emptyReg);
            recentCard.getChildren().addAll(recentHeader, emptyBox);
        } else {
            TableView<Evidence> recentTable = new TableView<>();
            recentTable.setPrefHeight(180);

            TableColumn<Evidence, String> idCol = new TableColumn<>("EVIDENCE ID");
            idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEvidenceId()));
            idCol.setPrefWidth(110);

            TableColumn<Evidence, String> caseCol = new TableColumn<>("CASE ID");
            caseCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCaseId()));
            caseCol.setPrefWidth(120);

            TableColumn<Evidence, String> typeCol = new TableColumn<>("TYPE");
            typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEvidenceType()));
            typeCol.setPrefWidth(150);

            TableColumn<Evidence, String> collCol = new TableColumn<>("COLLECTOR");
            collCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCollector()));
            collCol.setPrefWidth(140);

            TableColumn<Evidence, String> timeCol = new TableColumn<>("RECORDED AT");
            timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRecordedAt()));
            timeCol.setPrefWidth(150);

            TableColumn<Evidence, String> hashCol = new TableColumn<>("SHA-256 HASH (PREVIEW)");
            hashCol.setCellValueFactory(d -> {
                String h = d.getValue().getSha256Hash();
                return new SimpleStringProperty(h != null && h.length() > 16 ? h.substring(0, 10) + "..." + h.substring(h.length() - 8) : h);
            });
            hashCol.setPrefWidth(170);

            recentTable.getColumns().addAll(idCol, caseCol, typeCol, collCol, timeCol, hashCol);
            recentTable.getItems().setAll(recentEvidence);

            recentTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !recentTable.getSelectionModel().isEmpty()) {
                    openEvidenceDetails(recentTable.getSelectionModel().getSelectedItem());
                }
            });

            recentCard.getChildren().addAll(recentHeader, recentTable);
        }
        return recentCard;
    }

    private VBox buildRecentCustodyCard(List<CustodyEvent> recentCustody) {
        VBox custodyCard = new VBox(14);
        custodyCard.getStyleClass().add("soc-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("RECENT CUSTODY EVENTS (REAL SQLITE AUDIT TRAIL)");
        title.getStyleClass().add("soc-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAllCustodyBtn = new Button("View Custody Timeline →");
        viewAllCustodyBtn.getStyleClass().add("btn-secondary");
        viewAllCustodyBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 10px;");
        viewAllCustodyBtn.setOnAction(e -> showScreen("Chain of Custody"));

        header.getChildren().addAll(title, spacer, viewAllCustodyBtn);

        if (recentCustody.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(24));
            Label emptyLbl = new Label("No custody events recorded yet.");
            emptyLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
            emptyBox.getChildren().add(emptyLbl);
            custodyCard.getChildren().addAll(header, emptyBox);
        } else {
            TableView<CustodyEvent> table = new TableView<>();
            table.setPrefHeight(180);

            TableColumn<CustodyEvent, String> idCol = new TableColumn<>("EVIDENCE ID");
            idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEvidenceId()));
            idCol.setPrefWidth(110);

            TableColumn<CustodyEvent, String> actionCol = new TableColumn<>("ACTION");
            actionCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAction()));
            actionCol.setPrefWidth(130);

            TableColumn<CustodyEvent, String> fromCol = new TableColumn<>("FROM");
            fromCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActorFrom()));
            fromCol.setPrefWidth(140);

            TableColumn<CustodyEvent, String> toCol = new TableColumn<>("TO");
            toCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getActorTo()));
            toCol.setPrefWidth(140);

            TableColumn<CustodyEvent, String> timeCol = new TableColumn<>("TIMESTAMP");
            timeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp()));
            timeCol.setPrefWidth(150);

            TableColumn<CustodyEvent, String> notesCol = new TableColumn<>("NOTES");
            notesCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotes()));
            notesCol.setPrefWidth(200);

            table.getColumns().addAll(idCol, actionCol, fromCol, toCol, timeCol, notesCol);
            table.getItems().setAll(recentCustody);

            custodyCard.getChildren().addAll(header, table);
        }
        return custodyCard;
    }

    private void renderEvidenceList() {
        pageTitleLabel.setText("Evidence Catalog");
        pageSubtitleLabel.setText("Browse, search, and inspect all registered digital evidence items stored in SQLite.");

        EvidenceListView listView = new EvidenceListView(
                evidenceService,
                this::openEvidenceDetails,
                () -> showScreen("Register Evidence")
        );
        contentArea.getChildren().add(listView.createView());
    }

    private void renderRegisterEvidence() {
        pageTitleLabel.setText("Register Digital Evidence");
        pageSubtitleLabel.setText("Ingest digital media, compute cryptographic SHA-256 fingerprint, and persist to SQLite.");

        RegisterEvidenceView registerView = new RegisterEvidenceView(
                evidenceService,
                evidence -> {
                    updateHeaderStatus();
                    this.selectedEvidenceForDetails = evidence;
                },
                () -> showScreen("Evidence")
        );
        contentArea.getChildren().add(registerView.createView());
    }

    private void renderEvidenceDetails() {
        pageTitleLabel.setText("Evidence Details & Metadata");
        pageSubtitleLabel.setText("Deep forensic metadata inspection, file integrity verification, and custody history.");

        if (selectedEvidenceForDetails == null) {
            try {
                List<Evidence> list = evidenceService.getRecentEvidence(1);
                if (!list.isEmpty()) {
                    selectedEvidenceForDetails = list.get(0);
                }
            } catch (SQLException ignored) {
            }
        }

        if (selectedEvidenceForDetails == null) {
            VBox emptyBox = new VBox(16);
            emptyBox.getStyleClass().add("soc-card");
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));

            Label lbl = new Label("No evidence item selected.");
            lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px;");

            Button browseBtn = new Button("Go to Evidence Catalog →");
            browseBtn.getStyleClass().add("btn-primary");
            browseBtn.setOnAction(e -> showScreen("Evidence"));

            emptyBox.getChildren().addAll(lbl, browseBtn);
            contentArea.getChildren().add(emptyBox);
            return;
        }

        EvidenceDetailView detailView = new EvidenceDetailView(
                selectedEvidenceForDetails,
                evidenceService,
                () -> showScreen("Evidence")
        );
        contentArea.getChildren().add(detailView.createView());
    }

    private void renderCustodyView() {
        pageTitleLabel.setText("Chain of Custody History");
        pageSubtitleLabel.setText("Chronological custody event timeline and transfer tracking between forensic analysts.");

        CustodyView custodyView = new CustodyView(evidenceService);
        contentArea.getChildren().add(custodyView.createView());
    }

    private void renderLedgerView() {
        pageTitleLabel.setText("Blockchain-Style Hash-Linked Ledger");
        pageSubtitleLabel.setText("Audit immutable hash-linked blocks maintaining evidence state transitions.");

        LedgerView ledgerView = new LedgerView(evidenceService.getLedgerService());
        contentArea.getChildren().add(ledgerView.createView());
    }

    private void renderIntegrityVerificationView() {
        pageTitleLabel.setText("Cryptographic Integrity Verification");
        pageSubtitleLabel.setText("Verify evidence files against disk hashes and detect physical bitstream tampering.");

        IntegrityVerificationView verificationView = new IntegrityVerificationView(
                evidenceService,
                this::openEvidenceDetails
        );
        contentArea.getChildren().add(verificationView.createView());
    }

    private Node createKpiCard(String title, String value, String meta, String accentColor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("soc-card");
        card.setStyle(card.getStyle() + "; -fx-border-color: #1e293b; -fx-border-left-color: " + accentColor + "; -fx-border-left-width: 4px;");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("soc-card-title");

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("soc-card-value");

        Label metaLbl = new Label(meta);
        metaLbl.getStyleClass().add("soc-card-meta");

        card.getChildren().addAll(titleLbl, valLbl, metaLbl);
        return card;
    }
}
