import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultimediaAssist extends Application {

    private final ImageView imageView = new ImageView();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isPlaying = false;
    private double minFreq = 200;
    private double maxFreq = 4000;

    // Load OpenCV library
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Video to Sound Converter");

        // Main layout
        BorderPane root = new BorderPane();
        root.setCenter(imageView);

        // Toolbar setup
        ToolBar toolBar = createToolBar(primaryStage);
        root.setTop(toolBar);

        // Scene setup
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ToolBar createToolBar(Stage primaryStage) {
        ToolBar toolBar = new ToolBar();

        // Control buttons
        Button openFileButton = new Button("Open File");
        Button playPauseButton = new Button("Play");
        Button stopButton = new Button("Stop");

        openFileButton.setOnAction(e -> openFile(primaryStage));
        playPauseButton.setOnAction(e -> togglePlayPause(playPauseButton));
        stopButton.setOnAction(e -> stopPlayback());

        // Frequency sliders
        Slider minFreqSlider = new Slider(100, 1000, minFreq);
        Slider maxFreqSlider = new Slider(2000, 10000, maxFreq);
        Label minFreqLabel = new Label("Min Freq: " + (int) minFreq);
        Label maxFreqLabel = new Label("Max Freq: " + (int) maxFreq);

        // Add listeners to sliders
        minFreqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            minFreq = newVal.doubleValue();
            minFreqLabel.setText("Min Freq: " + (int) minFreq);
        });
        maxFreqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            maxFreq = newVal.doubleValue();
            maxFreqLabel.setText("Max Freq: " + (int) maxFreq);
        });

        // Add components to the toolbar
        toolBar.getItems().addAll(openFileButton, playPauseButton, stopButton, minFreqLabel, minFreqSlider, maxFreqLabel, maxFreqSlider);
        return toolBar;
    }

    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Video or Image File");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            if (isPlaying) {
                stopPlayback();
            }
            processFile(file);
        }
    }

    private void processFile(File file) {
        String filePath = file.getAbsolutePath();
        if (filePath.endsWith(".mp4") || filePath.endsWith(".avi")) {
            playVideo(filePath);
        } else if (filePath.endsWith(".png") || filePath.endsWith(".jpg")) {
            playImage(filePath);
        } else {
            showAlert("Unsupported File", "The selected file format is not supported.");
        }
    }

    private void playVideo(String filePath) {
        isPlaying = true;
        executor.submit(() -> {
            VideoCapture videoCapture = new VideoCapture(filePath);
            Mat frame = new Mat();

            try {
                while (isPlaying && videoCapture.read(frame)) {
                    BufferedImage bufferedImage = matToBufferedImage(frame);
                    javafx.application.Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null)));

                    SoundGenerator.playSineWaveFromImage(bufferedImage);

                    Thread.sleep(1000); // Play at 1 frame per second
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                videoCapture.release();
            }
        });
    }

    private void playImage(String filePath) {
        Mat mat = Imgcodecs.imread(filePath);
        if (mat.empty()) {
            showAlert("Load Error", "Failed to load the selected image file.");
            return;
        }

        BufferedImage image = matToBufferedImage(mat);
        imageView.setImage(SwingFXUtils.toFXImage(image, null));
        isPlaying = true;

        executor.submit(() -> {
            try {
                while (isPlaying) {
                    SoundGenerator.playSineWaveFromImage(image);
                    Thread.sleep(1000); // Loop every second
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        Mat convertedMat = new Mat();
        Imgproc.cvtColor(mat, convertedMat, Imgproc.COLOR_BGR2GRAY);
        int width = convertedMat.width();
        int height = convertedMat.height();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = new byte[width * height];
        convertedMat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    private void togglePlayPause(Button playPauseButton) {
        isPlaying = !isPlaying;
        playPauseButton.setText(isPlaying ? "Pause" : "Play");
    }

    private void stopPlayback() {
        isPlaying = false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
