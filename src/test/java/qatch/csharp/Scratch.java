package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.xml.sax.SAXException;
import qatch.analysis.IAnalyzer;
import qatch.analysis.IFindingsResultsImporter;
import qatch.analysis.IMetricsResultsImporter;
import qatch.calibration.BenchmarkProjects;
import qatch.calibration.RInvoker;
import qatch.evaluation.Project;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Scratch {

    @Test
    public void scratch() throws IOException {

        File file = null;

        URL res = RInvoker.getRScriptResource(RInvoker.Script.THRESHOLD);
        if (res.getProtocol().equalsIgnoreCase("jar")) {
            InputStream input = res.openStream();
            file = File.createTempFile("thresholdsExtractor", ".R", new File("src/test/output"));
            OutputStream out = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];

            while ((read = input.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.close();
            file.deleteOnExit();
        }

        if (file != null && !file.exists()) {
            throw new RuntimeException("Error: File " + file + " not found!");
        }

        System.out.println("testing...");
    }

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
