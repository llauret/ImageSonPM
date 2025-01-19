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

import java.util.function.Consumer;

public class Interface {
    private static final double DEFAULT_MIN_FREQ = 200;
    private static final double DEFAULT_MAX_FREQ = 4000;

    double minFreq = DEFAULT_MIN_FREQ;
    double maxFreq = DEFAULT_MAX_FREQ;

    private final ImageView imageView = new ImageView();
    private final MediaProcessor mediaProcessor = new MediaProcessor(imageView, DEFAULT_MIN_FREQ, DEFAULT_MAX_FREQ);
    private final FileHandler fileHandler = new FileHandler(mediaProcessor);

    public VBox createImageViewPane() {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getStyleClass().add("image-view-pane");
        imageView.setFitWidth(900);
        imageView.setPreserveRatio(true);
        vbox.getChildren().add(imageView);
        return vbox;
    }

    public ToolBar createToolBar(Stage stage) {
        ToolBar toolBar = new ToolBar();
        toolBar.setPadding(new Insets(15));
        toolBar.getStyleClass().add("toolbar");

        Button openFileButton = createStyledButton("\uD83D\uDCC1 Open File");
        Button playPauseButton = createStyledButton("▶ Play");
        Button stopButton = createStyledButton("⏹ Stop");

        Label minFreqLabel = new Label("Min Freq: " + (int) minFreq);
        minFreqLabel.getStyleClass().add("label");
        Slider minFreqSlider = createFrequencySlider(100, 1000, minFreq, minFreqLabel, "Min Freq: ");
        configureSlider(minFreqSlider, minFreqLabel, "Min Freq: ", minFreq, mediaProcessor::setMinFreq);

        Label maxFreqLabel = new Label("Max Freq: " + (int) maxFreq);
        maxFreqLabel.getStyleClass().add("label");
        Slider maxFreqSlider = createFrequencySlider(2000, 10000, maxFreq, maxFreqLabel, "Max Freq: ");
        configureSlider(maxFreqSlider, maxFreqLabel, "Max Freq: ", maxFreq, mediaProcessor::setMaxFreq);
        openFileButton.setOnAction(e -> fileHandler.openFile(stage));
        playPauseButton.setOnAction(e -> mediaProcessor.togglePlayPause(playPauseButton));
        stopButton.setOnAction(e -> mediaProcessor.stopPlayback());

        HBox sliders = new HBox(30, minFreqLabel, minFreqSlider, maxFreqLabel, maxFreqSlider);
        sliders.setAlignment(Pos.CENTER);
        sliders.setPadding(new Insets(20));
        sliders.getStyleClass().add("sliders");

        toolBar.getItems().addAll(openFileButton, playPauseButton, stopButton);
        VBox controlBox = new VBox(20, toolBar, sliders);
        controlBox.setPadding(new Insets(25));
        controlBox.getStyleClass().add("control-box");

        return new ToolBar(new HBox(controlBox));
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button");
        return button;
    }

    private Slider createFrequencySlider(double min, double max, double initialValue, Label label, String labelPrefix) {
        Slider slider = new Slider(min, max, initialValue);
        slider.getStyleClass().add("slider");
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            label.setText(labelPrefix + (int) newVal.doubleValue());
        });
        return slider;
    }

    private void configureSlider(Slider slider, Label label, String labelPrefix, double initialValue, Consumer<Double> updateAction) {
        slider.setValue(initialValue);
        label.setText(labelPrefix + (int) initialValue);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            label.setText(labelPrefix + (int) value);
            updateAction.accept(value);
        });
    }
}
