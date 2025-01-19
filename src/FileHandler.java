import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileHandler {
        private final MediaProcessor mediaProcessor;
        public FileHandler(MediaProcessor mediaProcessor) {
            this.mediaProcessor = mediaProcessor;
        }

        void openFile(Stage stage) {
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
            mediaProcessor.stopPlayback();
            mediaProcessor.restartExecutor();
            processFile(file);
        }

        private void processFile(File file) {
            String filePath = file.getAbsolutePath();
            if (filePath.matches(".*\\.(mp4|avi)$")) {
                mediaProcessor.playVideo(filePath);
            } else if (filePath.matches(".*\\.(png|jpg)$")) {
                mediaProcessor.playImage(filePath);
            }
        }
}
