import javax.sound.sampled.*;
import java.awt.image.BufferedImage;

public class SoundGenerator {

    public static void playSineWaveFromImage(BufferedImage image, double minFreq, double maxFreq) {
        try {
            BufferedImage grayscaleImage = resizeAndGrayscale(image, 64, 64);

            generateSoundFromImage(grayscaleImage, minFreq, maxFreq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage resizeAndGrayscale(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        resizedImage.getGraphics().drawImage(image, 0, 0, width, height, null);
        return resizedImage;
    }

    private static void generateSoundFromImage(BufferedImage image, double minFreq, double maxFreq) throws LineUnavailableException {
        int width = image.getWidth();
        int height = image.getHeight();

        double[] frequencies = new double[height];
        for (int i = 0; i < height; i++) {
            frequencies[i] = minFreq * Math.pow(maxFreq / minFreq, (double) i / (height - 1));
        }

        byte[] soundBuffer = new byte[(int) (44100 * 1000 / 1000)];

        for (int x = 0; x < width; x++) {
            double[] amplitudes = new double[height];
            for (int y = 0; y < height; y++) {
                int grayLevel = (image.getRGB(x, y) & 0xFF) / 16;
                amplitudes[y] = grayLevel / 15.0;
            }

            generateColumnSound(soundBuffer, frequencies, amplitudes, x, width);
        }

        addClickSound(soundBuffer);

        playSound(soundBuffer);
    }

    private static void generateColumnSound(byte[] buffer, double[] frequencies, double[] amplitudes, int column, int totalColumns) {
        int startSample = column * buffer.length / totalColumns;
        int endSample = (column + 1) * buffer.length / totalColumns;

        for (int t = startSample; t < endSample; t++) {
            double time = t / 44100.0;
            double value = 0;

            for (int i = 0; i < frequencies.length; i++) {
                value += amplitudes[i] * Math.sin(2 * Math.PI * frequencies[i] * time);
            }

            buffer[t] += (byte) (value * 127 / frequencies.length);
        }
    }

    private static void addClickSound(byte[] buffer) {
        int clickDurationSamples = (int) (44100 * 0.05); // 50 ms pour le clic
        for (int i = 0; i < clickDurationSamples; i++) {
            buffer[buffer.length - clickDurationSamples + i] = (byte) (Math.sin(2 * Math.PI * 1000 * i / 44100.0) * 127);
        }
    }

    private static void playSound(byte[] buffer) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 8, 1, true, true);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));

        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }
}
