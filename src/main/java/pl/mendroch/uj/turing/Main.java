package pl.mendroch.uj.turing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import pl.mendroch.uj.turing.view.MainWindowController;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Log
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Logger parent = log.getParent();
        FileHandler handler = new FileHandler("log.txt", true);
        handler.setFormatter(new SimpleFormatter());
        parent.addHandler(handler);
        log.info("Application started");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("mainWindow.fxml"));
        Parent root = loader.load();
        MainWindowController controller = loader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(root, 300, 275);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        stage.setOnCloseRequest(event -> {
            controller.close();
            handler.close();
        });
        stage.setTitle("Turing Machine");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setAlwaysOnTop(false);
        stage.show();
    }
}
