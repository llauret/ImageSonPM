import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;

public class ImageGenerator {
    public static void main(String[] args) {
        int width = 64;
        int height = 64;
        int numShapes = 5;
        int numImages = 100;
        String outputDir = "generated_images";

        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdir();
        }

        Random random = new Random();
        for (int i = 1; i <= numImages; i++) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = image.createGraphics();

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            for (int j = 0; j < numShapes; j++) {
                g2d.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                int x1 = random.nextInt(width);
                int y1 = random.nextInt(height);
                int x2 = random.nextInt(width);
                int y2 = random.nextInt(height);

                int xMin = Math.min(x1, x2);
                int xMax = Math.max(x1, x2);
                int yMin = Math.min(y1, y2);
                int yMax = Math.max(y1, y2);

                switch (random.nextInt(3)) {
                    case 0 -> g2d.drawLine(x1, y1, x2, y2);
                    case 1 -> g2d.drawRect(xMin, yMin, xMax - xMin, yMax - yMin);
                    case 2 -> g2d.drawOval(xMin, yMin, xMax - xMin, yMax - yMin);
                }
            }

            g2d.dispose();

            String fileName = String.format("%s/image_%03d.png", outputDir, i);
            try {
                ImageIO.write(image, "png", new File(fileName));
                System.out.println("Image générée : " + fileName);
            } catch (IOException e) {
                System.err.println("Erreur lors de l'écriture de l'image : " + fileName);
                e.printStackTrace();
            }
        }
    }
}
