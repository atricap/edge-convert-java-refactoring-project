import java.io.File;
import java.util.Objects;

public class UnrecognizedFileFormatException extends Exception {
   private final File file;

   public UnrecognizedFileFormatException(File file) {
      super("Unrecognized file format for file: " + Objects.requireNonNull(file).getName());
      this.file = file;
   }

   public File getFile() {
      return file;
   }
}
