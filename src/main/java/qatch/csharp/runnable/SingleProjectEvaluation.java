package qatch.csharp.runnable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qatch.analysis.*;
import qatch.csharp.*;
import qatch.runnable.SingleProjectEvaluator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

// TODO: lots of todos to think about in runner regarding best approach for configuration strings
public class SingleProjectEvaluation {

    // parameter constants
    private static File ROOT =        new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString()).getParentFile();
    // TODO: discuss having QM file packaged and referenced with runner or referenced via config file
    private static File RESOURCES =   new File(ROOT, "src/main/resources");
    // TODO: discuss how to deal with potentially different tools locations due to differences in JAR runs and multi-project runs
    private static File TOOLS =       new File(RESOURCES, "tools");
    private static Logger logger =    LoggerFactory.getLogger(SingleProjectEvaluation.class);


    /**
     * Main method for running quality evaluation on a single C# project or solution.
     *
     * @param args configuration array in following order:
     *             0: path to config file. See the config.properties file in src/test/resources/config for an example.
     *             1: (optional) path to resources folder if not using default location. This is currently
     *                necessary for JAR runs when copying tools and quality models out of resources folder.
     *    These arg paths can be relative or full path.
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

        Properties properties = new Properties();
        String propertiesConfigFilePath = args[0];
        try (InputStream input = new FileInputStream(propertiesConfigFilePath)) {
            properties.load(input);
            PROJECT_DIR = Paths.get(properties.getProperty("project.root"));
            RESULTS_DIR = Paths.get(properties.getProperty("results.directory"));
            QM_LOCATION = Paths.get(properties.getProperty("qm.filepath"));
            MS_BUILD = Paths.get(properties.getProperty("msbuild.bin"));
        }

        // Create output directory if not existing yet
        String resultsDirName = FilenameUtils.getBaseName(PROJECT_DIR.getFileName().toString());
        RESULTS_DIR = new File(RESULTS_DIR.toString(), resultsDirName).toPath();
        RESULTS_DIR.toFile().mkdirs();

        // Instantiate interface classes
        logger.debug("Beginning tool instantiations");
        ITool roslynator = new Roslynator(
                "Roslynator",
                TOOLS.toPath(),
                MS_BUILD
        );
        IToolLOC loc = new LocTool("RoslynatorLOC", TOOLS.toPath(), MS_BUILD);
        logger.trace("Analyzers loaded");

        // Run evaluation
        logger.debug("BEGINNING SINGLE PROJECT EVALUATION");
        logger.debug("Analyzing project: {}", PROJECT_DIR.toString());
        Path evalResults = new SingleProjectEvaluator().runEvaluator(PROJECT_DIR, RESULTS_DIR, QM_LOCATION, roslynator, loc);
        logger.info("Evaluation finished. You can find the results at {}", evalResults.toString());
    }


    /**
     * Initialize results directory and handle input parameters
     *
     * @param inputArgs project and results location as described in main method
     * @return HashMap containing paths of analysis project and results folder
     */
    @Deprecated
    private static HashMap<String, Path> initialize(String[] inputArgs) {

        String projectLoc = inputArgs[0];
        String resultsLoc = inputArgs[1];
        Path qmLoc = Paths.get(inputArgs[2]);
        String resources = (inputArgs.length < 4 ? null : inputArgs[3]);

        Path projectDir = new File(projectLoc).toPath();

        String resultsDirName = FilenameUtils.getBaseName(projectDir.getFileName().toString());
        Path qaDir = new File(resultsLoc, resultsDirName).toPath();
        qaDir.toFile().mkdirs();

        HashMap<String, Path> paths = new HashMap<>();
        paths.put("projectLoc", projectDir);
        paths.put("resultsLoc", qaDir);
        paths.put("qmLoc", qmLoc);
        if (resources != null) paths.put("resources", Paths.get(resources));

        return paths;
    }

}
