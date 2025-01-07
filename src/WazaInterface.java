import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WazaInterface extends Application {

    private ImageView imageView;
    private Slider timeSlider;
    private Button playPauseButton;
    private boolean isPlaying = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Stylish Image Interface");

        BorderPane root = new BorderPane();
        VBox controlsBox = new VBox(10);
        controlsBox.setPadding(new Insets(10));
        HBox playerControls = new HBox(10);
        playerControls.setPadding(new Insets(10));

        imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        Button selectImageButton = new Button("Select Image");
        selectImageButton.setOnAction(e -> selectImage(primaryStage));

        playPauseButton = new Button("Play");
        playPauseButton.setOnAction(e -> togglePlayPause());

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetPlayer());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> cancelSelection());

        timeSlider = new Slider();
        timeSlider.setMin(0);
        timeSlider.setMax(100);
        timeSlider.setValue(0);

        playerControls.getChildren().addAll(playPauseButton, resetButton, timeSlider);
        controlsBox.getChildren().addAll(selectImageButton, playerControls, cancelButton);
        root.setCenter(imageView);
        root.setBottom(controlsBox);

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void selectImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                // Afficher l'image dans l'interface
                Image image = new Image(selectedFile.toURI().toString());
                imageView.setImage(image);

                // Charger l'image en tant que BufferedImage pour traitement
                BufferedImage bufferedImage = ImageIO.read(selectedFile);

                // Jouer le son basé sur l'image
                // SoundGenerator.playSineWaveFromImage(bufferedImage);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Erreur lors du chargement de l'image : " + ex.getMessage());
            }
        }
    }

    private void togglePlayPause() {
        if (isPlaying) {
            playPauseButton.setText("Play");
            isPlaying = false;
            // Logic to pause time-slider updates
        } else {
            playPauseButton.setText("Pause");
            isPlaying = true;
            // Logic to start time-slider updates
        }
    }

    private void resetPlayer() {
        timeSlider.setValue(0);
        playPauseButton.setText("Play");
        isPlaying = false;
        // Logic to reset playback state
    }

    private void cancelSelection() {
        imageView.setImage(null);
        resetPlayer();
    }
}
