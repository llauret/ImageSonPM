import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Interface {
    private static final double DEFAULT_MIN_FREQ = 200;
    private static final double DEFAULT_MAX_FREQ = 4000;

    private double minFreq = DEFAULT_MIN_FREQ;
    private double maxFreq = DEFAULT_MAX_FREQ;

    private final ImageView imageView = new ImageView();
    private final MediaProcessor mediaProcessor = new MediaProcessor(imageView, DEFAULT_MIN_FREQ, DEFAULT_MAX_FREQ);
    private final FileHandler fileHandler = new FileHandler(mediaProcessor);

    public VBox createImageViewPane() {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));
        imageView.setFitWidth(800);
        imageView.setPreserveRatio(true);
        vbox.getChildren().add(imageView);
        return vbox;
    }

    public ToolBar createToolBar(Stage stage) {
        ToolBar toolBar = new ToolBar();
        toolBar.setPadding(new Insets(5));
        toolBar.setStyle("-fx-background-color: #f4f4f4;");

        Button openFileButton = new Button("Open File");
        Button playPauseButton = new Button("Play");
        Button stopButton = new Button("Stop");

        Label minFreqLabel = new Label("Min Freq: " + (int) minFreq);
        Slider minFreqSlider = createFrequencySlider(100, 1000, minFreq, minFreqLabel, "Min Freq: ");
        minFreqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            minFreq = newVal.doubleValue();
            minFreqLabel.setText("Min Freq: " + (int) minFreq);
            mediaProcessor.setMinFreq(minFreq);
        });

        Label maxFreqLabel = new Label("Max Freq: " + (int) maxFreq);
        Slider maxFreqSlider = createFrequencySlider(2000, 10000, maxFreq, maxFreqLabel, "Max Freq: ");
        maxFreqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            maxFreq = newVal.doubleValue();
            maxFreqLabel.setText("Max Freq: " + (int) maxFreq);
            mediaProcessor.setMaxFreq(maxFreq);
        });

        openFileButton.setOnAction(e -> fileHandler.openFile(stage));
        playPauseButton.setOnAction(e -> mediaProcessor.togglePlayPause(playPauseButton));
        stopButton.setOnAction(e -> mediaProcessor.stopPlayback());

        HBox sliders = new HBox(10, minFreqLabel, minFreqSlider, maxFreqLabel, maxFreqSlider);
        sliders.setAlignment(Pos.CENTER);
        sliders.setPadding(new Insets(5));

        toolBar.getItems().addAll(openFileButton, playPauseButton, stopButton);
        VBox controlBox = new VBox(10, toolBar, sliders);
        controlBox.setPadding(new Insets(10));

        return new ToolBar(new HBox(controlBox));
    }

    private Slider createFrequencySlider(double min, double max, double initialValue, Label label, String labelPrefix) {
        Slider slider = new Slider(min, max, initialValue);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            label.setText(labelPrefix + (int) newVal.doubleValue());
        });
        return slider;
    }
}
