package qatch.csharp.runnable;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import qatch.analysis.*;
import qatch.csharp.*;
import qatch.evaluation.EvaluationResultsExporter;
import qatch.evaluation.Project;
import qatch.evaluation.ProjectCharacteristicsEvaluator;
import qatch.evaluation.ProjectEvaluator;
import qatch.model.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;

public class SingleProjectEvaluation {

    // parameter constants
    private static final File ROOT = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString()).getParentFile();
    private static final File QM_LOCATION = new File(ROOT + "/src/main/resources/models/qualityModel_iso25k_csharp.xml");
    public static final File TOOLS_LOCATION = new File(ROOT + "/src/main/resources/tools");

    /**
     * Main method for running quality evaluation on a single C# project.
     *
     * @param args configuration array in following order:
     *             0: path to project to be evaluated root folder
     *             1: path to folder to place results
     *    These arg paths can be relative or full path
     */
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, CloneNotSupportedException {

        // TODO: use logger instead of println
        System.out.println("******************************  Project Evaluator *******************************");
        System.out.println();

        // Initialize
        if (args == null || args.length < 2) {
            throw new RuntimeException("Incorrect input parameters given. Be sure to include \n\t(0) Path to root directory of "
                    + "project to analyze, \n\t(1) Path to directory to place analysis results.");
        }
        HashMap<String, Path> initializePaths = initialize(args);
        final Path projectDir = initializePaths.get("projectLoc");
        final Path resultsDir = initializePaths.get("resultsLoc");


        /*
         * Step 0 : Load the desired Quality Model
         */
        // TODO: eventually move all main runnable steps into qatch framework to be called in language agnostic ways
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
        project.setName(projectDir.getFileName().toString());

        System.out.println("* Project Name : " + project.getName());
        System.out.println("* Project Path : " + project.getPath());
        System.out.println("*");
        System.out.println("* Project successfully loaded..!");


        /*
         * Step 2: Analyze the desired project against the selected properties
         */
        System.out.println("\n**************** STEP 2: Project Analyzer ****************************");
        System.out.println("*");
        System.out.println("* Analyzing the desired project");
        System.out.println("* Please wait...");
        System.out.println("*");

        checkCreateClearDirectory(resultsDir.toFile());

        //Instantiate the available single project analyzers of the system ...
        IAnalyzer metricsAnalyzer = new LOCMetricsAnalyzer(TOOLS_LOCATION.toPath());
        IAnalyzer findingsAnalyzer = new FxcopAnalyzer();

        metricsAnalyzer.analyze(projectDir, resultsDir, qualityModel.getProperties());
        findingsAnalyzer.analyze(projectDir, resultsDir, qualityModel.getProperties());

        //Print some messages to the user
        System.out.println("* The analysis is finished");
        System.out.println("* You can find the results at : " + resultsDir.toAbsolutePath().toString());
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
        File[] results = resultsDir.toFile().listFiles();

        //For each result file found in the directory do...
        // TODO: this functionality will eventually be moved to a qatch-min generic ResultsImporter class
        if (results == null) throw new RuntimeException("Scanner results directory [" + resultsDir.toString() + "] has no files");
        for(File resultFile : results){

            //Check if it is not a LOCMetrics result file
            if(!resultFile.getName().contains("LocMetrics")) {
                //Parse the issues and add them to the IssueSet Vector of the Project object
                project.addIssueSet(findingsImporter.parse(resultFile.toPath()));
            }else{
                //Parse the metrics of the project and add them to the MetricSet field of the Project object
                project.setMetrics(metricsImporter.parse(resultFile.toPath()));
            }
        }

        // Print some informative messages to the console
        System.out.println("*");
        System.out.println("* The results of the static analysis are successfully imported ");


        /*
         * Step 4 : Aggregate the static analysis results of the desired project
         */
        System.out.println("\n**************** STEP 4: Aggregation Process *************************");

        //Print some messages
        System.out.println("*");
        System.out.println("* Aggregating the results of the project...");
        System.out.println("* I.e. Calculating the normalized values of its properties...");
        System.out.println("* Please wait...");
        System.out.println("*");

        //Clone the properties of the quality model to the properties of the certain project
        for(int i = 0; i < qualityModel.getProperties().size(); i++){
            //Clone the property and add it to the PropertySet of the current project
            Property p = (Property) qualityModel.getProperties().get(i).clone();
            project.addProperty(p);
        }

        IMetricsAggregator metricsAggregator = new LOCMetricsAggregator();
        IFindingsAggregator findingsAggregator = new FxcopAggregator();

        //Aggregate all the analysis results
        metricsAggregator.aggregate(project);
        findingsAggregator.aggregate(project);

        //Normalize their values
        for(int i = 0; i < project.getProperties().size(); i++){
            Property property =  project.getProperties().get(i);
            property.getMeasure().calculateNormValue();
        }

        System.out.println("*");
        System.out.println("* Aggregation process finished..!");


        /*
         * STEP 5 : Evaluate all the benchmark projects against their thresholds.
         */

        System.out.println("\n**************** STEP 5: Properties Evaluation ***********************");
        System.out.println("*");
        System.out.println("* Evaluating the project's properties against the calculated thresholds...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        //Create a single project property evaluator
        ProjectEvaluator evaluator = new ProjectEvaluator();

        //Evaluate all its properties
        evaluator.evaluateProjectProperties(project);

        System.out.println("*");
        System.out.println("* The project's properties successfully evaluated..!");

        /*
         * Step 6 : Evaluate the project's characteristics
         */

        System.out.println("\n**************** STEP 6: Characteristics Evaluation ******************");
        System.out.println("*");
        System.out.println("* Evaluating the project's characteristics based on the eval values of its properties...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        //Clone the quality model characteristics inside the project
        //For each quality model's characteristic do...
        for(int i = 0; i < qualityModel.getCharacteristics().size(); i++){
            //Clone the characteristic and add it to the CharacteristicSet of the current project
            Characteristic c = (Characteristic) qualityModel.getCharacteristics().get(i).clone();
            project.getCharacteristics().addCharacteristic(c);
        }

        //Create a single project property evaluator
        ProjectCharacteristicsEvaluator charEvaluator = new ProjectCharacteristicsEvaluator();

        //Evaluate the project's characteristics
        charEvaluator.evaluateProjectCharacteristics(project);

        System.out.println("*");
        System.out.println("* The project's characteristics successfully evaluated..!");

        /*
         * Step 7 : Calculate the TQI of the project
         */

        System.out.println("\n**************** STEP 7: TQI Calculation *****************************");
        System.out.println("*");
        System.out.println("* Calgculating the TQI of the project ...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        //Copy the TQI object of the QM to the tqi field of this project
        project.setTqi((Tqi)qualityModel.getTqi().clone());

        //Calculate the project's TQI
        project.calculateTQI();

        System.out.println("*");
        System.out.println("* The TQI of the project successfully evaluated..!");


        /*
         * Step 8 : Export the project's data and properties in a json file
         */
        System.out.println("\n**************** STEP 8: Exporting Evaluation Results ****************");
        System.out.println("*");
        System.out.println("* Exporting the results of the project evaluation...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        EvaluationResultsExporter.exportProjectToJson(
                project,
                new File(resultsDir.toString() + File.separator + project.getName() + "_evalResults.json")
                        .getAbsolutePath()
        );

        System.out.println("* Results successfully exported..!");
        System.out.println("* You can find the results at : " + resultsDir.toString());

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

        Path projectDir = new File(projectLoc).toPath();

        String resultsDirName = projectDir.getFileName().toString();
        Path qaDir = new File(resultsLoc, resultsDirName).toPath();
        qaDir.toFile().mkdirs();

        HashMap<String, Path> paths = new HashMap<>();
        paths.put("projectLoc", projectDir);
        paths.put("resultsLoc", qaDir);

        return paths;
    }

}
