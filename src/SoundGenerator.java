import javax.sound.sampled.*;
import java.awt.image.BufferedImage;

public class SoundGenerator {

    public static void playSineWaveFromImage(BufferedImage image) {
        try {
            // Réduire la résolution de l'image et convertir en niveaux de gris
            BufferedImage grayscaleImage = resizeAndGrayscale(image, 64, 64);

            // Générer un son à partir de l'image
            generateSoundFromImage(grayscaleImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage resizeAndGrayscale(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        resizedImage.getGraphics().drawImage(image, 0, 0, width, height, null);
        return resizedImage;
    }

    private static void generateSoundFromImage(BufferedImage image) throws LineUnavailableException {
        int width = image.getWidth();
        int height = image.getHeight();

        // Fréquences associées aux pixels (logarithmique entre 200 Hz et 4000 Hz)
        double[] frequencies = new double[height];
        double minFreq = 200;
        double maxFreq = 4000;
        for (int i = 0; i < height; i++) {
            frequencies[i] = minFreq * Math.pow(maxFreq / minFreq, (double) i / (height - 1));
        }

        byte[] soundBuffer = new byte[(int) (44100 * 1000 / 1000)];

        // Générer le son colonne par colonne
        for (int x = 0; x < width; x++) {
            double[] amplitudes = new double[height];
            for (int y = 0; y < height; y++) {
                int grayLevel = (image.getRGB(x, y) & 0xFF) / 16; // Niveaux de gris réduits à 4 bits
                amplitudes[y] = grayLevel / 15.0; // Amplitude normalisée
            }

            // Créer un mélange sonore pour la colonne
            generateColumnSound(soundBuffer, frequencies, amplitudes, x, width);
        }

        // Ajouter un clic sonore à la fin
        addClickSound(soundBuffer);

        // Lecture du son
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

            // Normalisation et conversion en byte
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
