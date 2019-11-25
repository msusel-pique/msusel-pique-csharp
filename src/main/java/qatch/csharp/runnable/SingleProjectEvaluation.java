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
    private static String ROSLYN_NAME = "Roslynator";


    /**
     * Main method for running quality evaluation on a single C# project or solution.
     *
     * @param args configuration array in following order:
     *             0: path to project to be evaluated root folder
     *             1: path to folder to place results
     *             2: path to quality model file
     *             3: (optional) path to resources folder if not using default location. This is currently
     *                necessary for JAR runs when copying tools and quality models out of resources folder.
     *    These arg paths can be relative or full path.
     */
    public static void main(String[] args) throws IOException {

        // initialize config
        logger.debug("Beginning initilization phase");
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException("Incorrect input parameters given. Be sure to include " +
                    "\n\t(0) Path to root directory of project to analyze, " +
                    "\n\t(1) Path to directory to place analysis results," +
                    "\n\t(3) (optional) Path to resources location.");
        }
        HashMap<String, Path> initializePaths = initialize(args);
        Path PROJECT_DIR = initializePaths.get("projectLoc");
        Path RESULTS_DIR = initializePaths.get("resultsLoc");
        Path QM_LOCATION = initializePaths.get("qmLoc");

        if (args.length >= 4) {     // temp fix for JAR runs, deal with later
            RESOURCES = new File(initializePaths.get("resources").toString());
//            QM_LOCATION = new File(RESOURCES, "models/qualityModel_security_csharp.xml");
            TOOLS = new File(RESOURCES, "tools");
        }

        Properties properties = new Properties();
        // TODO (maybe): Find source control friendly way to deal with MSBuild location property.
        properties.load((new FileInputStream("src/test/resources/config/config.properties")));

        // instantiate interface classes
        logger.debug("Beginning interface instantiations");
        ITool roslynator = new Roslynator(
                ROSLYN_NAME,
                TOOLS.toPath(),
                Paths.get(properties.getProperty("MSBUILD_BIN"))
        );
        IToolLOC loc = new LocTool("RoslynatorLOC", TOOLS.toPath(), Paths.get(properties.getProperty("MSBUILD_BIN")));
        logger.trace("Analyzers loaded");

        // run evaluation
        logger.debug("BEGINNING SINGLE PROJECT EVALUATION");
        logger.debug("Analyzing project: {}", PROJECT_DIR.toString());
        Path evalResults = new SingleProjectEvaluator().runEvaluator(PROJECT_DIR, RESULTS_DIR, QM_LOCATION, roslynator, loc);
        logger.info("Evaluation finished. You can find the results at {}", evalResults.toString());
    }

    /**
     * Cleans directory. Creates if it does not exist.
     * @param dir File object of directory to create or clear
     */
    private static void checkCreateClearDirectory(File dir){

        //Check if the directory exists
        if(!dir.isDirectory() || !dir.exists()) dir.mkdirs();

        //Clear previous results
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Initialize results directory and handle input parameters
     *
     * @param inputArgs project and results location as described in main method
     * @return HashMap containing paths of analysis project and results folder
     */
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
