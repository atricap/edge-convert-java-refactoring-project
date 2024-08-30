import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EdgeConvertFileParserTest {

    public static final String EDGE_FILE_NAME = "src/test/resources/Courses.edg";

    @Test
    public void characterize() {
        File edgeFile = new File(EDGE_FILE_NAME).getAbsoluteFile();
        EdgeConvertFileParser parser = new EdgeConvertFileParser(edgeFile);

        EdgeTable[] tables = parser.getEdgeTables();
        for (EdgeTable table : tables) {
            table.makeArrays();
        }

        assertEquals("[Table: 1\n{\nTableName: STUDENT\nNativeFields: 7|8\nRelatedTables: \nRelatedFields: 0|0\n}\n, Table: 2\n{\nTableName: FACULTY\nNativeFields: 11|6\nRelatedTables: 13\nRelatedFields: 0|0\n}\n, Table: 13\n{\nTableName: COURSES\nNativeFields: 3|5\nRelatedTables: 2\nRelatedFields: 0|0\n}\n]", Arrays.toString(tables));
    }
}
