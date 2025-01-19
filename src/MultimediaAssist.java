import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.util.Objects;

public class MultimediaAssist extends Application {

    Interface uiInterface = new Interface();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Video to Sound Converter");
        BorderPane root = new BorderPane();
        root.setCenter(uiInterface.createImageViewPane());

        ToolBar toolBar = uiInterface.createToolBar(primaryStage);
        root.setTop(toolBar);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
