package pique.csharp;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {

    private static final Path TEST_DIR = new File("src/test").toPath();
    private static final Path TEST_OUT_A = Paths.get(TEST_DIR.toString(), "out").toAbsolutePath();
    private static final Path TEST_OUT_B = Paths.get(TEST_DIR.toString(),"resources", "out").toAbsolutePath();
    private static final Path ROOT_OUT = Paths.get("out").toAbsolutePath();
    private static final Path RESOURCES = Paths.get("resources").toAbsolutePath();

    public static void cleanTestOutput() throws IOException {
        if (TEST_OUT_A.toFile().exists()) FileUtils.forceDelete(TEST_OUT_A.toFile());
        if (TEST_OUT_B.toFile().exists()) FileUtils.forceDelete(TEST_OUT_B.toFile());
        if (ROOT_OUT.toFile().exists()) FileUtils.forceDelete(ROOT_OUT.toFile());
        if (RESOURCES.toFile().exists()) FileUtils.forceDelete(RESOURCES.toFile());
    }

}
