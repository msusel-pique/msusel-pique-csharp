package qatch.csharp.runnable;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qatch.analysis.*;
import qatch.csharp.*;
import qatch.runnable.SingleProjectEvaluator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class SingleProjectEvaluation {

    // parameter constants
    private static final File ROOT = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString()).getParentFile();
    // TODO: discuss having QM file packaged and referenced with runner or referenced via config file
    private static File resources = new File(ROOT + "/src/main/resources");
    private static File qmLocation = new File(resources, "models/qualityModel_iso25k_csharp.xml");
    private static File tools = new File(resources, "tools");
    // TODO: discuss how to deal with potentially different tools locations due to differences in JAR runs and multi-project runs

    private static final Logger logger = LoggerFactory.getLogger(SingleProjectEvaluation.class);


    /**
     * Main method for running quality evaluation on a single C# project.
     *
     * @param args configuration array in following order:
     *             0: path to project to be evaluated root folder
     *             1: path to folder to place results
     *             2: (optional) path to resources folder if not using default location. This is currently
     *                necessary for JAR runs when copying tools and quality models out of resources folder.
     *    These arg paths can be relative or full path
     */
    public static void main(String[] args) {

        // initialize
        logger.debug("Beginning initilization phase");
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("Incorrect input parameters given. Be sure to include " +
                    "\n\t(0) Path to root directory of project to analyze, " +
                    "\n\t(1) Path to directory to place analysis results," +
                    "\n\t(2) (optional) Path to resources location.");
        }
        HashMap<String, Path> initializePaths = initialize(args);
        final Path projectDir = initializePaths.get("projectLoc");
        final Path resultsDir = initializePaths.get("resultsLoc");
        if (args.length >= 3) {
            resources = new File(initializePaths.get("resources").toString());
            qmLocation = new File(resources, "models/qualityModel_iso25k_csharp.xml");
            tools = new File(resources, "tools");
        }


        // instantiate interface classes
        logger.debug("Beginning interface instantiations");
        IAnalyzer metricsAnalyzer = new LOCMetricsAnalyzer(tools.toPath());
        IAnalyzer findingsAnalyzer = new FxcopAnalyzer(tools.toPath());
        logger.trace("Analyzers loaded");

        IMetricsResultsImporter metricsImporter = new LOCMetricsResultsImporter();
        IFindingsResultsImporter findingsImporter = new FxcopResultsImporter();
        logger.trace("ResultsImporters loaded");

        IMetricsAggregator metricsAggregator = new LOCMetricsAggregator();
        IFindingsAggregator findingsAggregator = new FxcopAggregator();
        logger.trace("Aggregators loaded");


        // run evaluation
        logger.debug("BEGINNING SINGLE PROJECT EVALUATION");
        logger.debug("Analyzing project: {}", projectDir.toString());
        Path evalResults = new SingleProjectEvaluator().runEvaluator(
                projectDir, resultsDir, qmLocation.toPath(), metricsAnalyzer,
                findingsAnalyzer, metricsImporter, findingsImporter,
                metricsAggregator, findingsAggregator
        );
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
        String resources = (inputArgs.length < 3 ? null : inputArgs[2]);

        Path projectDir = new File(projectLoc).toPath();
        String resultsDirName = projectDir.getFileName().toString();
        Path qaDir = new File(resultsLoc, resultsDirName).toPath();
        qaDir.toFile().mkdirs();

        HashMap<String, Path> paths = new HashMap<>();
        paths.put("projectLoc", projectDir);
        paths.put("resultsLoc", qaDir);
        if (resources != null) paths.put("resources", Paths.get(resources));

        return paths;
    }

}
