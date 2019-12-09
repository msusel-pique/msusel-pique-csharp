package qatch.csharp.runnable;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qatch.analysis.ITool;
import qatch.analysis.IToolLOC;
import qatch.csharp.RoslynatorLoc;
import qatch.csharp.RoslynatorAnalyzer;
import qatch.runnable.SingleProjectEvaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

// TODO: lots of todos to think about in runner regarding best approach for configuration strings
public class SingleProjectEvaluation {

    // parameter constants
    private static File ROOT =        new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString()).getParentFile();
    // TODO: discuss having QM file packaged and referenced with runner or referenced via config file
    private static File RESOURCES =   new File(ROOT, "src/main/resources");
    private static Path ROSLYN_RESOURCE_ROOT = Paths.get(RESOURCES.toString(), "Roslynator");
    private static Logger logger =    LoggerFactory.getLogger(SingleProjectEvaluation.class);


    /**
     * Main method for running quality evaluation on a single C# project or solution.
     *
     * @param args configuration array:
     *             0: path to config file. See the single_project_evaluation.properties.properties file in src/test/resources/config for an example.
     */
    public static void main(String[] args) throws IOException {

        // Initialize config
        logger.debug("Beginning initilization phase");

        Path PROJECT_DIR;
        Path RESULTS_DIR;
        Path QM_LOCATION;
        Path MS_BUILD;

        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Incorrect input parameters given. Be sure to include " +
                    "\n\t(0) Path to config file. See the config.properties file in src/test/resources/config for an example., " +
                    "\n\t(1) (optional) Path to resources location.,");
        }
        if (!FilenameUtils.getExtension(args[0]).equals("properties")) {
            throw new IllegalArgumentException("Incorrect input parameter given.\n"
                    + "Arg[0] should end with filetype .properties");
        }

        Properties properties = new Properties();
        String propertiesConfigFilePath = args[0];
        try (InputStream input = new FileInputStream(propertiesConfigFilePath)) {
            properties.load(input);
            PROJECT_DIR = Paths.get(properties.getProperty("project.root"));
            RESULTS_DIR = Paths.get(properties.getProperty("results.directory"));
            QM_LOCATION = Paths.get(properties.getProperty("qm.filepath"));
            MS_BUILD = Paths.get(properties.getProperty("msbuild.bin"));
        }

        // Validate input strings

        // Create output directory if not existing yet
        String resultsDirName = FilenameUtils.getBaseName(PROJECT_DIR.getFileName().toString());
        RESULTS_DIR = new File(RESULTS_DIR.toString(), resultsDirName).toPath();
        RESULTS_DIR.toFile().mkdirs();

        // Instantiate interface classes
        logger.debug("Beginning tool instantiations");
        ITool roslynator = new RoslynatorAnalyzer(
                ROSLYN_RESOURCE_ROOT,
                MS_BUILD
        );
        IToolLOC loc = new RoslynatorLoc(ROSLYN_RESOURCE_ROOT, MS_BUILD);
        logger.trace("Analyzers loaded");

        // Run evaluation
        logger.debug("BEGINNING SINGLE PROJECT EVALUATION");
        logger.debug("Analyzing project: {}", PROJECT_DIR.toString());
        Path evalResults = new SingleProjectEvaluator().runEvaluator(PROJECT_DIR, RESULTS_DIR, QM_LOCATION, roslynator, loc);
        logger.info("Evaluation finished. You can find the results at {}", evalResults.toString());
    }
}
