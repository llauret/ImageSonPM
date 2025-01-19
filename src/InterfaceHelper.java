import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class InterfaceHelper {
    private static final double MIN_FREQ = 200;
    private static final double MAX_FREQ = 4000;
    private final ImageView imageView = new ImageView();


    private boolean isPlaying = false;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private VideoCapture videoCapture;
    private static final Logger logger = Logger.getLogger(Interface.class.getName());

    private void openFile(Stage stage) {
        File file = chooseFile(stage);
        if (file != null) {
            handleNewFile(file);
        }
    }

    private File chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Video or Image File");
        return fileChooser.showOpenDialog(stage);
    }

    private void handleNewFile(File file) {
        stopPlayback();
        restartExecutor();
        processFile(file);
    }


    private void processFile(File file) {
        String filePath = file.getAbsolutePath();
        if (filePath.matches(".*\\.(mp4|avi)$")) {
            playVideo(filePath);
        } else if (filePath.matches(".*\\.(png|jpg)$")) {
            playImage(filePath);
        }
    }

    private void playVideo(String filePath) {
        isPlaying = true;
        videoCapture = new VideoCapture(filePath);
        executor.scheduleAtFixedRate(() -> {
            if (videoCapture.isOpened()) {
                Mat frame = new Mat();
                if (videoCapture.read(frame)) {
                    BufferedImage bufferedImage = matToBufferedImage(frame);
                    javafx.application.Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null)));
                    SoundGenerator.playSineWaveFromImage(bufferedImage, MIN_FREQ, MAX_FREQ);
                } else {
                    stopPlayback();
                }
            }
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    private void playImage(String filePath) {
        Mat mat = Imgcodecs.imread(filePath);
        if (mat.empty()) {
            logger.warning("Failed to load image: " + filePath);
            return;
        }
        BufferedImage image = matToBufferedImage(mat);
        imageView.setImage(SwingFXUtils.toFXImage(image, null));
        isPlaying = true;
        executor.scheduleAtFixedRate(() -> SoundGenerator.playSineWaveFromImage(image, MIN_FREQ, MAX_FREQ), 0, 1, TimeUnit.SECONDS);
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        Mat convertedMat = new Mat();
        Imgproc.cvtColor(mat, convertedMat, Imgproc.COLOR_BGR2GRAY);
        BufferedImage image = new BufferedImage(convertedMat.width(), convertedMat.height(), BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = new byte[(int) (convertedMat.total() * convertedMat.channels())];
        convertedMat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, convertedMat.width(), convertedMat.height(), data);
        return image;
    }

    private void togglePlayPause(Button playPauseButton) {
        isPlaying = !isPlaying;
        playPauseButton.setText(isPlaying ? "Pause" : "Play");
    }

    public void stopPlayback() {
        isPlaying = false;
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if (videoCapture != null) {
            videoCapture.release();
        }
    }

    private void restartExecutor() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
    }
}
