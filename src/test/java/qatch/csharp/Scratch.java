package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Scratch {

    @Test
    public void scratch() { }

    @Test
    public void tempDirectoryDelete() {
        try {
            Path tempDir = Files.createTempDirectory(Paths.get("."), "temp");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            List<String> lines = Arrays.asList("The first line", "The second line");
            Path file = Paths.get(tempDir.toString(), "the-file-name.txt");
            Files.write(file, lines, StandardCharsets.UTF_8);

            System.out.println(tempDir.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("testing...");

    }

    @Test
    public void testUserDirectory() throws IOException {
        File root = new File(System.getProperty("user.dir"));
        System.out.println(root.toString());
        System.out.println(root.getAbsolutePath());
        System.out.println(root.getCanonicalPath());
    }
}
