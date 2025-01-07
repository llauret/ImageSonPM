import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.LineUnavailableException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MultimediaAssist extends Application {

    private ImageView imageView = new ImageView();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isPlaying = false;
    private double minFreq = 200;
    private double maxFreq = 4000;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Video to Sound Converter");

        BorderPane root = new BorderPane();
        root.setCenter(imageView);

        // Controls
        ToolBar toolBar = new ToolBar();
        Button openFileButton = new Button("Open File");
        Button playPauseButton = new Button("Play");
        Button stopButton = new Button("Stop");
        toolBar.getItems().addAll(openFileButton, playPauseButton, stopButton);
        root.setTop(toolBar);

        // Frequency sliders
        Slider minFreqSlider = new Slider(100, 1000, minFreq);
        Slider maxFreqSlider = new Slider(2000, 10000, maxFreq);
        Label minFreqLabel = new Label("Min Freq: " + (int) minFreq);
        Label maxFreqLabel = new Label("Max Freq: " + (int) maxFreq);
        minFreqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            minFreq = newVal.doubleValue();
            minFreqLabel.setText("Min Freq: " + (int) minFreq);
        });
        maxFreqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            maxFreq = newVal.doubleValue();
            maxFreqLabel.setText("Max Freq: " + (int) maxFreq);
        });
        toolBar.getItems().addAll(minFreqLabel, minFreqSlider, maxFreqLabel, maxFreqSlider);

        openFileButton.setOnAction(e -> openFile(primaryStage));
        playPauseButton.setOnAction(e -> togglePlayPause(playPauseButton));
        stopButton.setOnAction(e -> stopPlayback());

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
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
        }
    }

    private void playVideo(String filePath) {
        isPlaying = true;
        executor.submit(() -> {
            VideoCapture videoCapture = new VideoCapture(filePath);
            Mat frame = new Mat();
            while (isPlaying && videoCapture.read(frame)) {
                BufferedImage bufferedImage = matToBufferedImage(frame);
                javafx.application.Platform.runLater(() -> {
                    imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                });

                SoundGenerator.playSineWaveFromImage(bufferedImage);

                try {
                    Thread.sleep(1000); // 1 frame per second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            videoCapture.release();
        });
    }

    private void playImage(String filePath) {
        Mat mat = Imgcodecs.imread(filePath);
        if (mat.empty()) {
            System.err.println("Failed to load image: " + filePath);
            return;
        }
        BufferedImage image = matToBufferedImage(mat);
        imageView.setImage(SwingFXUtils.toFXImage(image, null));
        isPlaying = true;
        executor.submit(() -> {
            while (isPlaying) {
                try {
                    SoundGenerator.playSineWaveFromImage(image);
                    Thread.sleep(1000); // Loop every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

    public static void main(String[] args) {
        launch(args);
    }
}
