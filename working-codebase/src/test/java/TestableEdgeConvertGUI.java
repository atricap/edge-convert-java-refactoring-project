import java.io.File;
import java.util.Optional;

public class TestableEdgeConvertGUI extends EdgeConvertGUI {

    @Override
    protected Optional<File> openEdgeFile() {
        return Optional.of(new File(EdgeConvertFileParserTest.EDGE_FILE_NAME));
    }
}
