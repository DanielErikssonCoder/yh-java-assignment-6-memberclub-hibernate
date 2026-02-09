package com.eriksson.rentalsystemhibernate3.ui;

import com.eriksson.rentalsystemhibernate3.util.HibernateUtil;
import com.eriksson.rentalsystemhibernate3.util.WindowResizer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image; // Ny import
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    /**
     * Initializes UI components, sets resizability, shows stage with fade transition
     */
    @Override
    public void start(Stage stage) throws IOException {

        HibernateUtil.getSessionFactory();

        try {
            Font.loadFont("https://fonts.gstatic.com/s/urbanist/v15/L0xjDF02iFML4hGCyOCnHTo2cl06.ttf", 12);
            Font.loadFont("https://fonts.gstatic.com/s/urbanist/v15/L0xkDF02iFML4hGCyOCnHTo2dFxX.ttf", 12);
            Font.loadFont("https://fonts.gstatic.com/s/urbanist/v15/L0xkDF02iFML4hGCyOCnHTo2BVzX.ttf", 12);

        } catch (Exception e) {
            System.out.println("Kunde inte ladda typsnitt.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/eriksson/rentalsystemhibernate3/main-view.fxml"));
        Parent root = fxmlLoader.load();

        scene = new Scene(root, 1280, 800);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Wigell Rental System");

        var iconStream = App.class.getResourceAsStream("/images/app-icon.png");

        // Checks for app icon and logs load status
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
            System.out.println("Ikon laddad!");

        } else {
            System.out.println("Kunde inte hitta ikonen under: /images/app-icon.png");
        }

        stage.setScene(scene);

        WindowResizer.addResizability(stage, root);

        stage.show();

        root.setOpacity(0);
        FadeTransition appFade = new FadeTransition(Duration.millis(800), root);
        appFade.setToValue(1);
        appFade.play();
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/eriksson/rentalsystemhibernate3/" + fxml + ".fxml"));
        scene.setRoot(fxmlLoader.load());
    }

    public static void main(String[] args) {
        launch();
    }
}