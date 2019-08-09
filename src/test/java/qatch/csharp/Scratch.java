package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.xml.sax.SAXException;
import qatch.analysis.IAnalyzer;
import qatch.analysis.IFindingsResultsImporter;
import qatch.analysis.IMetricsResultsImporter;
import qatch.calibration.BenchmarkProjects;
import qatch.evaluation.Project;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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

        BenchmarkProjects projects = new BenchmarkProjects();
        IMetricsResultsImporter mri = new LOCMetricsResultsImporter();
        IFindingsResultsImporter fri = new FxcopResultsImporter();

        Files.list(Paths.get("C:\\Users\\davidrice3\\Repository\\msusel-qatch\\msusel-qatch-csharp\\out\\benchmark_results"))
            .forEach(p -> {
                Project project = new Project(p.getFileName().toString());
                project.setPath(p.toAbsolutePath().toString());
                // parse and set metrics and issues found by the tools
                Path metricsFolder = Paths.get(p.toString(), "metrics");
                Path findingsFolder = Paths.get(p.toString(), "findings");

                try {
                    Files.list(metricsFolder)
                            .filter(Files::isRegularFile)
                            .forEach(f -> {
                                try { project.setMetrics(mri.parse(f));}
                                catch (IOException e) {	e.printStackTrace(); }
                            });
                } catch (IOException e) {e.printStackTrace(); }

                try {
                    Files.list(findingsFolder)
                            .filter(Files::isRegularFile)
                            .forEach(f -> {
                                try { project.addIssueSet(fri.parse(f)); }
                                catch (IOException | ParserConfigurationException | SAXException e) {
                                    e.printStackTrace();
                                }
                            });
                } catch (IOException e) { e.printStackTrace(); }

                projects.addProject(project);
            });


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
