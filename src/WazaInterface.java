//import javafx.application.Application;
//import javafx.embed.swing.SwingFXUtils;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.Slider;
//import javafx.scene.control.ToolBar;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import org.opencv.core.Core;
//import org.opencv.core.Mat;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.videoio.VideoCapture;
//
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//public class MultimediaAssist extends Application {
//
//    private final ImageView imageView = new ImageView();
//    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//    private boolean isPlaying = false;
//    private double minFreq = 200;
//    private double maxFreq = 4000;
//
//    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Video to Sound Converter");
//        BorderPane root = new BorderPane();
//        root.setCenter(createImageViewPane());
//
//        ToolBar toolBar = createToolBar(primaryStage);
//        root.setTop(toolBar);
//
//        Scene scene = new Scene(root, 1000, 700);
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    private VBox createImageViewPane() {
//        VBox vbox = new VBox();
//        vbox.setAlignment(Pos.CENTER);
//        vbox.setPadding(new Insets(10));
//        vbox.getChildren().add(imageView);
//        imageView.setFitWidth(800);
//        imageView.setPreserveRatio(true);
//        return vbox;
//    }
//
//    private ToolBar createToolBar(Stage stage) {
//        ToolBar toolBar = new ToolBar();
//        toolBar.setPadding(new Insets(5));
//        toolBar.setStyle("-fx-background-color: #f4f4f4;");
//
//        Button openFileButton = new Button("Open File");
//        Button playPauseButton = new Button("Play");
//        Button stopButton = new Button("Stop");
//
//        Slider minFreqSlider = new Slider(100, 1000, minFreq);
//        Slider maxFreqSlider = new Slider(2000, 10000, maxFreq);
//
//        Label minFreqLabel = new Label("Min Freq: " + (int) minFreq);
//        Label maxFreqLabel = new Label("Max Freq: " + (int) maxFreq);
//
//        minFreqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
//            minFreq = newVal.doubleValue();
//            minFreqLabel.setText("Min Freq: " + (int) minFreq);
//        });
//
//        maxFreqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
//            maxFreq = newVal.doubleValue();
//            maxFreqLabel.setText("Max Freq: " + (int) maxFreq);
//        });
//
//        openFileButton.setOnAction(e -> openFile(stage));
//        playPauseButton.setOnAction(e -> togglePlayPause(playPauseButton));
//        stopButton.setOnAction(e -> stopPlayback());
//
//        HBox sliders = new HBox(10, minFreqLabel, minFreqSlider, maxFreqLabel, maxFreqSlider);
//        sliders.setAlignment(Pos.CENTER);
//        sliders.setPadding(new Insets(5));
//
//        toolBar.getItems().addAll(openFileButton, playPauseButton, stopButton);
//        VBox controlBox = new VBox(10, toolBar, sliders);
//        controlBox.setPadding(new Insets(10));
//        return new ToolBar(new HBox(controlBox));
//    }
//
//    private void openFile(Stage stage) {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Open Video or Image File");
//        File file = fileChooser.showOpenDialog(stage);
//        if (file != null) {
//            if (isPlaying) {
//                stopPlayback();
//            }
//            processFile(file);
//        }
//    }
//
//    private void processFile(File file) {
//        String filePath = file.getAbsolutePath();
//        if (filePath.matches(".*\\.(mp4|avi)$")) {
//            playVideo(filePath);
//        } else if (filePath.matches(".*\\.(png|jpg)$")) {
//            playImage(filePath);
//        }
//    }
//
//    private void playVideo(String filePath) {
//        isPlaying = true;
//        executor.scheduleAtFixedRate(() -> {
//            VideoCapture videoCapture = new VideoCapture(filePath);
//            try {
//                Mat frame = new Mat();
//                if (videoCapture.read(frame)) {
//                    BufferedImage bufferedImage = matToBufferedImage(frame);
//                    javafx.application.Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null)));
//                    SoundGenerator.playSineWaveFromImage(bufferedImage, minFreq, maxFreq);
//                } else {
//                    stopPlayback();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                videoCapture.release();
//            }
//        }, 0, 1, TimeUnit.SECONDS);
//    }
//
//    private void playImage(String filePath) {
//        Mat mat = Imgcodecs.imread(filePath);
//        if (mat.empty()) {
//            System.err.println("Failed to load image: " + filePath);
//            return;
//        }
//        BufferedImage image = matToBufferedImage(mat);
//        imageView.setImage(SwingFXUtils.toFXImage(image, null));
//        isPlaying = true;
//        executor.scheduleAtFixedRate(() -> SoundGenerator.playSineWaveFromImage(image, minFreq, maxFreq), 0, 1, TimeUnit.SECONDS);
//    }
//
//    private BufferedImage matToBufferedImage(Mat mat) {
//        Mat convertedMat = new Mat();
//        Imgproc.cvtColor(mat, convertedMat, Imgproc.COLOR_BGR2GRAY);
//        BufferedImage image = new BufferedImage(convertedMat.width(), convertedMat.height(), BufferedImage.TYPE_BYTE_GRAY);
//        byte[] data = new byte[convertedMat.width() * convertedMat.height()];
//        convertedMat.get(0, 0, data);
//        image.getRaster().setDataElements(0, 0, convertedMat.width(), convertedMat.height(), data);
//        return image;
//    }
//
//    private void togglePlayPause(Button playPauseButton) {
//        isPlaying = !isPlaying;
//        playPauseButton.setText(isPlaying ? "Pause" : "Play");
//    }
//
//    private void stopPlayback() {
//        isPlaying = false;
//        executor.shutdownNow();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
