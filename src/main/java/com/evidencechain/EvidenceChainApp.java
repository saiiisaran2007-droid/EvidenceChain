package com.evidencechain;

import com.evidencechain.controller.MainController;
import com.evidencechain.database.DatabaseManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Main JavaFX Application class for EvidenceChain.
 * Handles database bootstrapping and UI shell initialization.
 */
public class EvidenceChainApp extends Application {

    private static final String APP_TITLE = "EvidenceChain — Digital Forensics Evidence Management";
    private static final double DEFAULT_WIDTH = 1120;
    private static final double DEFAULT_HEIGHT = 720;

    @Override
    public void init() {
        // Automatic database initialization on first launch
        System.out.println("[EvidenceChainApp] Initializing SQLite database...");
        DatabaseManager.initializeDatabase();
    }

    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController();

        Scene scene = new Scene(controller.getRootLayout(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Apply dark forensic stylesheet
        URL cssResource = getClass().getResource("/com/evidencechain/css/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("[EvidenceChainApp] Warning: style.css not found in classpath.");
        }

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        System.out.println("[EvidenceChainApp] UI Shell launched successfully.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
