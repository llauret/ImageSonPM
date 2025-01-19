import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MediaProcessor {
    private static final double MIN_FREQ = 200;
    private static final double MAX_FREQ = 4000;

    private double minFreq = MIN_FREQ;
    private double maxFreq = MAX_FREQ;

    private ImageView imageView = new ImageView();
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private boolean isPlaying = false;
    private VideoCapture videoCapture;
    private static final Logger logger = Logger.getLogger(Interface.class.getName());

    public MediaProcessor(ImageView imageView, double minFreq, double maxFreq) {
        this.imageView = imageView;
        this.minFreq = minFreq;
        this.maxFreq = maxFreq;
    }

    void playVideo(String filePath) {
        isPlaying = true;
        videoCapture = new VideoCapture(filePath);
        executor.scheduleAtFixedRate(() -> {
            if (videoCapture.isOpened()) {
                Mat frame = new Mat();
                if (videoCapture.read(frame)) {
                    BufferedImage bufferedImage = matToBufferedImage(frame);
                    javafx.application.Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null)));
                    SoundGenerator.playSineWaveFromImage(bufferedImage, minFreq, maxFreq);
                } else {
                    stopPlayback();
                }
            }
        }, 0, 33, TimeUnit.MILLISECONDS);
    }

    void playImage(String filePath) {
        Mat mat = Imgcodecs.imread(filePath);
        if (mat.empty()) {
            logger.warning("Failed to load image: " + filePath);
            return;
        }
        BufferedImage image = matToBufferedImage(mat);
        imageView.setImage(SwingFXUtils.toFXImage(image, null));
        isPlaying = true;
        executor.scheduleAtFixedRate(() -> SoundGenerator.playSineWaveFromImage(image, minFreq, maxFreq), 0, 1, TimeUnit.SECONDS);
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

    void togglePlayPause(Button playPauseButton) {
        isPlaying = !isPlaying;
        executor.shutdownNow();
        playPauseButton.setText(isPlaying ? "\u23F8 Pause" : "\u25B6 Play");
        if (isPlaying) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> SoundGenerator.playSineWaveFromImage(SwingFXUtils.fromFXImage(imageView.getImage(), null), minFreq, maxFreq), 0, 1, TimeUnit.SECONDS);
        } else {
            stopPlayback();
        }
    }

    public void stopPlayback() {
        isPlaying = false;
        executor.shutdownNow();
        if (videoCapture != null) {
            videoCapture.release();
        }
    }

    void restartExecutor() {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
    }

    public void setMinFreq(double minFreq) {
        this.minFreq = minFreq;
    }

    public void setMaxFreq(double maxFreq) {
        this.maxFreq = maxFreq;
    }

}
