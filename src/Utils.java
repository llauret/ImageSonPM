import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.opencv.core.Mat;

public class Utils {
    public static Image mat2Image(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        byte[] buffer = new byte[width * height * channels];
        mat.get(0, 0, buffer);

        if (channels == 1) {
            // Grayscale
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int gray = buffer[y * width + x] & 0xFF;
                    writer.setArgb(x, y, (255 << 24) | (gray << 16) | (gray << 8) | gray);
                }
            }
        } else if (channels == 3 || channels == 4) {
            // BGR(A)
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = (y * width + x) * channels;
                    int blue = buffer[index] & 0xFF;
                    int green = buffer[index + 1] & 0xFF;
                    int red = buffer[index + 2] & 0xFF;
                    writer.setArgb(x, y, (255 << 24) | (red << 16) | (green << 8) | blue);
                }
            }
        }

        return image;
    }
}
