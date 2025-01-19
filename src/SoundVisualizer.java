import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class SoundVisualizer {
    private final Canvas canvas;
    private final GraphicsContext gc;

    public SoundVisualizer(double width, double height) {
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void drawWaveform(byte[] audioBuffer) {
        if (audioBuffer == null) return;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.beginPath();
        double centerY = canvas.getHeight() / 2;
        for (int i = 0; i < audioBuffer.length; i++) {
            double x = i * canvas.getWidth() / audioBuffer.length;
            double y = centerY + (audioBuffer[i] / 127.0) * centerY;
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
    }
}
