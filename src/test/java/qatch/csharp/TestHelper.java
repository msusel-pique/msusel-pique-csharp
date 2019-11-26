package qatch.csharp;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {

    private static final Path TEST_DIR = new File("src/test").toPath();
    private static final Path OUTPUT = Paths.get(TEST_DIR.toString(), "out").toAbsolutePath();

    public static void clean(File dest) throws IOException {
        if (dest.exists()) { FileUtils.cleanDirectory(dest); }
        else dest.mkdirs();
    }

    public static void cleanTestOutput() throws IOException {
        FileUtils.forceDelete(OUTPUT.toFile());
        OUTPUT.toFile().mkdirs();
    }

}
