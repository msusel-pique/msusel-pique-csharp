package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import qatch.analysis.IAnalyzer;
import qatch.analysis.IFindingsResultsImporter;
import qatch.analysis.IMetricsResultsImporter;
import qatch.evaluation.Project;
import qatch.model.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;

public class SingleProjectEvaluation {

    // parameter constants
    public static final File ROOT = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString()).getParentFile();
    public static final File QM_LOCATION = new File(ROOT + "/src/main/resources/models/qualityModel_csharp.xml");
    public static final File TOOLS_LOCATION = new File(ROOT + "/src/main/resources/tools");

    /**
     * Main method for running quality evaluation on a single C# project.
     *
     * @param args configuration array in following order:
     *             0: path to project to be evaluated root folder
     *             1: path to folder to place results
     *    These arg paths can be relative or full path
     */
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {

        System.out.println("******************************  Project Evaluator *******************************");
        System.out.println();

        // Initialize
        if (args == null || args.length < 2) {
            throw new RuntimeException("Incorrect input parameters given. Be sure to include \n\t(1) Path to root directory of "
                    + "project to analyze, \n\t(2) Path to directory to place analysis results.");
        }
        HashMap<String, File> initializePaths = initialize(args);
        final File projectDir = initializePaths.get("projectLoc");
        final File resultsDir = initializePaths.get("resultsLoc");


        /*
         * Step 0 : Load the desired Quality Model
         */
        System.out.println("**************** STEP 0: Quality Model Loader ************************");
        System.out.println("*");
        System.out.println("* Loading the desired Quality Model...");
        System.out.println("* Please wait...");
        System.out.println("*");

        QualityModelLoader qmImporter = new QualityModelLoader(QM_LOCATION.getPath());
        QualityModel qualityModel = qmImporter.importQualityModel();

        System.out.println("* Quality Model successfully loaded..!");


        /*
         * Step 1: Create the Project object that simulates the desired project
         */
        System.out.println("\n**************** STEP 1: Project Loader ******************************");
        System.out.println("*");
        System.out.println("* Loading the desired project...");
        System.out.println("* Please wait...");
        System.out.println("*");


        //Create a Project object to store the results of the static analysis and the evaluation of this project...
        Project project = new Project();

        //Set the absolute path and the name of the project
        project.setPath(projectDir.toString());
        project.setName(projectDir.getName());

        System.out.println("* Project Name : " + project.getName());
        System.out.println("* Project Path : " + project.getPath());
        System.out.println("*");
        System.out.println("* Project successfully loaded..!");


        /*
         * Step 2: Analyze the desired project against the selected properties
         */
        checkCreateClearDirectory(resultsDir);

        System.out.println("\n**************** STEP 2: Project Analyzer ****************************");
        System.out.println("*");
        System.out.println("* Analyzing the desired project");
        System.out.println("* Please wait...");
        System.out.println("*");

        //Instantiate the available single project analyzers of the system ...
        IAnalyzer metricsAnalyzer = new LOCMetricsAnalyzer();
        IAnalyzer findingsAnalyzer = new FxcopAnalyzer();

        metricsAnalyzer.analyze(projectDir, resultsDir, qualityModel.getProperties());
        findingsAnalyzer.analyze(projectDir, resultsDir, qualityModel.getProperties());

        //Print some messages to the user
        System.out.println("* The analysis is finished");
        System.out.println("* You can find the results at : " + resultsDir.getAbsolutePath());
        System.out.println();


        System.out.println("\n**************** STEP 3: Results Importer ****************************");
        System.out.println("*");
        System.out.println("* Importing the results of the analysis...");
        System.out.println("* Please wait...");
        System.out.println("*");

        //Create Result Importers for each tool used
        IMetricsResultsImporter metricsImporter = new LOCMetricsResultsImporter();
        IFindingsResultsImporter findingsImporter = new FxcopResultsImporter();

        //Get the directory with the results of the analysis
        File[] results = resultsDir.listFiles();

        //For each result file found in the directory do...
        if (results == null) throw new RuntimeException("Scanner results directory [" + resultsDir.toString() + "] has no files");
        for(File resultFile : results){

            //Check if it is a LOCMetrics result file
            if(!resultFile.getName().contains("LocMetrics")) {
                //Parse the issues and add them to the IssueSet Vector of the Project object
                project.addIssueSet(findingsImporter.parse(resultFile.getAbsolutePath()));
            }else{
                //Parse the metrics of the project and add them to the MetricSet field of the Project object
                project.setMetrics(metricsImporter.parse(resultFile.getAbsolutePath()));
            }
        }

        // Print some informative messages to the console
        System.out.println("*");
        System.out.println("* The results of the static analysis are successfully imported ");
        System.out.println("...");
    }

    /**
     * Cleans directory. Creates if it does not exist.
     * @param dir File object of directory to create or clear
     */
    static void checkCreateClearDirectory(File dir){

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
    static HashMap<String, File> initialize(String[] inputArgs) {

        String projectLoc = inputArgs[0];
        String resultsLoc = inputArgs[1];
        String resultsDirName = "qa-results";

        File projectDir = new File(projectLoc);
        File qaDir = new File(resultsLoc, resultsDirName);
        qaDir.mkdirs();

        HashMap<String, File> paths = new HashMap<>();
        paths.put("projectLoc", projectDir);
        paths.put("resultsLoc", qaDir);

        return paths;
    }

}
