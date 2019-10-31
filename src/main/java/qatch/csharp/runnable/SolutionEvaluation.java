package qatch.csharp.runnable;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import qatch.analysis.*;
import qatch.csharp.*;
import qatch.evaluation.EvaluationResultsExporter;
import qatch.evaluation.Project;
import qatch.evaluation.ProjectCharacteristicsEvaluator;
import qatch.evaluation.ProjectEvaluator;
import qatch.model.*;
import qatch.utility.FileUtility;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This executable class is responsible for producing quality analysis reports on all modules contained within
 * a C# solution (.sln).  This driver supports deployed JAR functionality when packaged with dependencies.
 */
@Deprecated
public class SolutionEvaluation {

    private final static Logger logger = LoggerFactory.getLogger(SolutionEvaluation.class);
    private final static Path ROOT = Paths.get(System.getProperty("user.dir"));
    /**
     * Run single project evaluations on a .NET Framework solution in batch mode to produce analysis results
     * for every .csproj project.  Assumes a derived c# quality model already exists.
     *
     * @param args configuration array in following order:
     *             0: path to the solution to be evaluated root folder
     *             1: path to folder to place analysis results
     *    These arg paths can be relative or full path
     */
    @Deprecated
    public static void main(String[] args) {

        // useful constants
        final Path SOLUTION;
        final Path OUTPUT;
        final Path RESOURCES;

        // TODO: discuss having QM file packaged and referenced with runner or referenced via config file
        final String QM_NAME = "qualityModel_iso25k_csharp.xml";
        final String projectRootFlag = ".csproj";   // how to know you are at a project root when recursing through files

        // initialize
        if (args == null || args.length != 2) {
            throw new RuntimeException("Incorrect input parameters given. Be sure to include " +
                    "\n\t(0) path to the solution to be evaluated root folder," +
                    "\n\t(1) path to folder to place analysis results.");
        }
        SOLUTION = Paths.get(args[0]);
        OUTPUT = Paths.get(args[1], "qa_out");

        OUTPUT.toFile().mkdirs();

        // extract resources
        RESOURCES = extractResources(OUTPUT);

        // run single project evaluation on each project found in the target solution folder
        logger.info("* * * * * * * * * * * * * * *");
        logger.info("* Beginning Qatch .NET quality analysis.");
        logger.info("* C# Solution being analyzed: {}", SOLUTION.toString());
        logger.info("* Output directory: {}", OUTPUT.toString());
        logger.info("* Active quality model: {}", QM_NAME);
        logger.info("* * * * * * * * * * * * * * *");

        Set<Path> projectRoots = FileUtility.multiProjectCollector(SOLUTION, projectRootFlag);
        logger.info("{} projects found for analysis.", projectRoots.size());

        projectRoots.forEach(p -> {
            logger.info("Beginning analysis on {}", p.getFileName());
            try {
                SingleProjectEvaluation.main(new String[] { p.toString(), OUTPUT.toString(), RESOURCES.toString() });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private static Path extractResources(Path destination)  {

        String protocol = SolutionEvaluation.class.getResource("").getProtocol();

        try {
            Path resourcesDirectory = Files.createTempDirectory(destination, "resources");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { FileUtils.deleteDirectory(resourcesDirectory.toFile()); }
                catch (IOException e) { e.printStackTrace(); }
            }));

            if (Objects.equals(protocol, "jar")) {
                try { extractResourcesToTempFolder(resourcesDirectory); }
                catch (IOException | URISyntaxException e) { e.printStackTrace(); }
            }

            else if (Objects.equals(protocol, "file")) {
                File models = new File(ROOT + "/src/main/resources/tools");
                File tools = new File(ROOT + "/src/main/resources/models");
                try {
                    FileUtils.copyDirectoryToDirectory(models , resourcesDirectory.toFile());
                    FileUtils.copyDirectoryToDirectory(tools , resourcesDirectory.toFile());
                }
                catch (IOException e) {  e.printStackTrace(); }
            }

            else { throw new RuntimeException("Unable to determine if project is running from IDE or JAR"); }

            return resourcesDirectory;

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("retrun statement in try block was never reached.");
    }


    /**
     * Code from https://stackoverflow.com/questions/1529611/how-to-write-a-java-program-which-can-extract-a-jar-file-and-store-its-data-in-s/1529707#1529707
     * by user Jamesst20
     *
     * Used when running program as a JAR.
     *
     * Takes resources in the resources folder within the JAR and copies them to a
     * resources folder in the same directory as the JAR. Also moves the ant build.xml
     * file to root directory.
     */
    private static void extractResourcesToTempFolder(Path destination) throws IOException, URISyntaxException {
        File jarFile = new File(SolutionEvaluation
                .class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());

        //Recursively build resources folder from JAR sibling to JAR file
        JarFile jar = new JarFile(jarFile.getPath());
        Enumeration<JarEntry> enums = jar.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().startsWith("models") || entry.getName().startsWith("tools")) {
                File toWrite = new File(destination.toFile(), entry.getName());
                if (entry.isDirectory()) {
                    toWrite.mkdirs();
                    continue;
                }
                InputStream in = new BufferedInputStream(jar.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWrite));
                byte[] buffer = new byte[2048];
                for (;;) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nBytes);
                }
                out.flush();
                out.close();
                in.close();
            }
        }
    }

}
