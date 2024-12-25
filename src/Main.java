import javafx.application.Application;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Main extends Application {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        System.out.println("Test waza");
        imageCharcutor3000("C:/Users/scinp/Images/ilovethemballs.png");
    }

    public static void imageCharcutor3000(String imagePath) {
        Mat srcImage = Imgcodecs.imread(imagePath);
        int centerX = srcImage.cols() / 2;
        int centerY = srcImage.rows() / 2;
        int cropSize = 64;

        int x = centerX - cropSize / 2;
        int y = centerY - cropSize / 2;

        Rect roi = new Rect(x, y, cropSize, cropSize);
        Mat croppedImage = new Mat(srcImage, roi);
        Imgproc.cvtColor(croppedImage, croppedImage, Imgproc.COLOR_BGR2GRAY);

        String projectRoot = System.getProperty("user.dir");
        String newImagePath = projectRoot + "/zoomed_image.jpg";

        Imgcodecs.imwrite(newImagePath, croppedImage);
        System.out.println("Image zoomed and saved: " + newImagePath);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // TODO Auto-generated method stub
    }
}