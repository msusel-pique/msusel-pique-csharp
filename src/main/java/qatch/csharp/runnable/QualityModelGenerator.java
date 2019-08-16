package qatch.csharp.runnable;

import qatch.analysis.IAnalyzer;
import qatch.calibration.*;
import qatch.csharp.*;
import qatch.model.*;
import qatch.utility.FileUtility;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class QualityModelGenerator {

    /**
     * Main method for deriving a C# quality model.
     * Elicites weights using an AHP matrix and determines thresholds using a benchmark repository.
     *
     * @param args configuration array in the following order:
     *             0: [true | false] execute benchmark calibration
     *             1: [true | false] rerun tool static analysis
     *             1: path of the benchmark repository directory. Can be relative or full path
     */
    public static void main(String[] args) {

        // constants
        final boolean RECALIBRATE;
        final boolean RERUN_TOOLS;
        final Path BENCH_REPO_PATH;
        final Path ROOT = Paths.get(System.getProperty("user.dir"));
        final Path OUTPUT = Paths.get(ROOT.toString(), "out");
        final Path QM_DESCRIPTION_PATH = Paths.get(ROOT.toString(), "src/main/resources/models/qualityModel_iso25k_csharp_description.xml");
        final String PROJ_ROOT_FLAG = ".csproj";    // identifies individual C# project (module) roots in the repo (at any depth)


        System.out.println("\n\n******************************  Model Generator *******************************");

        // Initialize
        if (args == null || args.length < 2) {
            throw new RuntimeException("Incorrect input parameters given. Be sure to include " +
                "\n\t(0) [true | false] flag for benchmark calibration," +
                "\n\t(1) [true | false] flag for rerunning of tool analysis," +
                "\n\t(2) location of benchmark repository");
        }
        HashMap<String, String> config = initialize(args);

        RECALIBRATE = Boolean.parseBoolean(config.get("recalibrate"));
        RERUN_TOOLS = Boolean.parseBoolean(config.get("rerunTools"));
        BENCH_REPO_PATH = Paths.get(config.get("benchRepoPath"));
        OUTPUT.toFile().mkdirs();

        System.out.println("* Benchmark Repository: " + BENCH_REPO_PATH.toString());
        System.out.println("* Output Directory: " + OUTPUT.toString());
        System.out.println("*");
        System.out.println("* Starting Analysis...");
        System.out.println("* Loading Quality Model...");

        // Set the properties and the characteristics
        QualityModel qualityModel = new PropertiesAndCharacteristicsLoader(QM_DESCRIPTION_PATH.toAbsolutePath().toString()).importQualityModel();
        PropertySet properties = qualityModel.getProperties();
        CharacteristicSet characteristics = qualityModel.getCharacteristics();
        Tqi tqi = qualityModel.getTqi();

        System.out.println("* Empty Quality Model successfully loaded...!");
        System.out.println("******************************");

        BenchmarkProjects projects = null;
        BenchmarkAnalysisExporter exporter = null;
        BenchmarkAnalyzer benchAnalyzer = new BenchmarkAnalyzer(properties, BENCH_REPO_PATH, OUTPUT);

        // Check if the user wants to execute a benchmark calibration
        if (RECALIBRATE) {

            // pre-run file system checks
            File r_dir = new File(RInvoker.R_WORK_DIR.toFile(), "Comparison_Matrices");
            File compMatrix = new File(r_dir, "TQI.xls");
            if (!r_dir.isDirectory() || !compMatrix.isFile()) {
                throw new RuntimeException("There must exist the hand-entered .xls comparison matrices in directory " +
                        r_dir.toString() + ".\nSee ComparisonMatricesCreator class for more information.");
            }

            /*
             * Step 1 : Analyze the projects found in the desired Benchmark Repository
             */
            if (RERUN_TOOLS) {
                System.out.println("\n**************** STEP 1 : Benchmark Analyzer *************************");
                System.out.println("*");
                System.out.println("* Analyzing the projects of the desired benchmark repository");
                System.out.println("* Please wait...");
                System.out.println("*");

                // actualize language-specific analyzers
                IAnalyzer metricsAnalyzer = new LOCMetricsAnalyzer();
                IAnalyzer findingsAnalyzer = new FxcopAnalyzer();

                //Start the analysis of the benchmark repository
                benchAnalyzer.analyzeBenchmarkRepo(metricsAnalyzer, findingsAnalyzer, PROJ_ROOT_FLAG);

                System.out.println("* You can find the results at : " + benchAnalyzer.getBENCH_RESULTS_PATH().toString());
                System.out.println("******************************");
                System.out.println();
            }
            else {
                System.out.println("\n**************** STEP 1 : Benchmark Analyzer *************************");
                System.out.println("*");
                System.out.println("* Skipping tool static analysis. Assuming results already exist.");
                System.out.println("******************************");
            }

            /*
             * Step 2 : Import the results of the analysis and store them into different objects
             */
            System.out.println("\n**************** STEP 2 : Benchmark Importer *************************");
            System.out.println("*");
            System.out.println("* Importing the results of the analysis...");
            System.out.println("* Please wait...");
            System.out.println("*");

            // Create an empty BenchmarkImporter
            BenchmarkResultImporter benchmarkImporter = new BenchmarkResultImporter();

            // Start importing the project results
            projects = benchmarkImporter.importResults(benchAnalyzer, new LOCMetricsResultsImporter(), new FxcopResultsImporter());

            // Print some informative messages to the console
            System.out.println("*");
            System.out.println("* The results are successfully imported..! ");
            System.out.println("******************************");


            /*
             * Step 3 : Aggregate the results of each project
             */
            System.out.println("\n**************** STEP 3: Aggregation Process *************************");
            System.out.println("*");
            System.out.println("* Aggregating the results of each project...");
            System.out.println("* I.e. Calculating the normalized values of their properties...");
            System.out.println("* Please wait...");
            System.out.println("*");

            // Create an empty BenchmarkAggregator and aggregate the metrics of the project
            BenchmarkAggregator benchAggregator = new BenchmarkAggregator();
            try {
                benchAggregator.aggregateProjects(projects, properties, new LOCMetricsAggregator(), new FxcopAggregator());
            }
            catch (CloneNotSupportedException e) { e.printStackTrace(); }

            System.out.println("*");
            System.out.println("* Aggregation process finished..!");
            System.out.println("******************************");

            /*
             * Step 4 : Export the benchmark analysis results for the R - Analysis
             */
            System.out.println("\n**************** STEP 4: Properties exportation for R analysis *******");
            System.out.println("*");

            // Create an analysis exporter and export the Properties in a xls form
            exporter = new BenchmarkAnalysisExporter();
            exporter.exportToXls(projects);

            System.out.println("*");
            System.out.println("* The xls file with the properties is successfully exported \n and placed into R's working directory!");

            /*
             * Step 5 : Invoke R analysis for the threshold calculation
             */
            System.out.println("\n**************** STEP 5: Threshold extraction ************************");
            System.out.println("*");
            System.out.println("* Calling R for threshold extraction...");
            System.out.println("* This will take a while...");
            System.out.println("*");

            // TODO: find way to have all non-language specific procedure occur in qatch framework module
            // get r threshold script from framework
            File rWorkingDir = new File(ROOT.toFile(), "r_working_directory");
            rWorkingDir.mkdirs();
            File tempThreshScript = FileUtility.tempFileCopyFromJar(RInvoker.getRScriptResource(RInvoker.Script.THRESHOLD), rWorkingDir.toPath());

            // Create an Empty R Invoker and execute the threshold extraction script
            RInvoker rInvoker = new RInvoker();
            rInvoker.executeRScript(RInvoker.R_BIN_PATH, tempThreshScript.toPath(), rWorkingDir.toString());

            System.out.println("*");
            System.out.println("* R analysis finished..!");
            System.out.println("* Thresholds exported in a JSON file..!");

            /*
             * Step 6 : Import the thresholds and assign them to each property of the Quality Model
             */
            System.out.println("\n**************** STEP 6: Importing the thresholds ********************");
            System.out.println("*");
            System.out.println("* Importing the thresholds from the JSON file into JVM...");
            System.out.println("* This will take a while...");
            System.out.println("*");

            // Create an empty Threshold importer and import the thresholds from the json file exported by R
            // The file is placed into the R working directory (fixed)
            ThresholdImporter thresholdImp = new ThresholdImporter();
            thresholdImp.importThresholdsFromJSON(properties);

            System.out.println("* Thresholds successfully imported..!");

            /*
             * Step 7 : Copy the thresholds to each projects' properties
             */
            System.out.println("\n**************** STEP 7: Loading thresholds to Projects  *************");
            System.out.println("*");
            System.out.println("* Seting the thresholds to the properties of the existing projects...");
            System.out.println("* This will take a while...");
            System.out.println("*");

            // TODO: move this functionality to Qatch framework
            // TODO: use functional approach, use dictionary mapping instead of by index
            for(int i = 0; i < projects.size(); i++){
                PropertySet prop = projects.getProject(i).getProperties();
                for(int j = 0; j < prop.size(); j++){
                    prop.get(j).setThresholds(properties.get(j).getThresholds().clone());
                }
            }

            System.out.println("*");
            System.out.println("* Thresholds successfully loaded to the projects' properties..!");

            System.out.println("\n*********************************** Weight Elicitation *************************************");
            System.out.println("*");
            System.out.println("*");

            // Call R script for weight elicitation
            // TODO: find way to have all non-language specific procedure occur in qatch framework module
            // get r threshold script from framework
            File tempWeightsScript = FileUtility.tempFileCopyFromJar(RInvoker.getRScriptResource(RInvoker.Script.AHP), rWorkingDir.toPath());

            // execute the weight elicitation script
            rInvoker.executeRScript(RInvoker.R_BIN_PATH, tempWeightsScript.toPath(), rWorkingDir.toString());

            // Import the weights from the json file
            WeightsImporter weightImporter = new WeightsImporter();
            weightImporter.importWeights(tqi, characteristics);

        }

        /*
         * STEP 8 : Export the properties in an XML (or JSON) File
         */
        System.out.println("\n**************** STEP 8: Exporting Results ***********************");
        System.out.println("*");
        System.out.println("* Exporting the results to XML, JSON files and JDOM Element object...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        QualityModelExporter qmExp = new QualityModelExporter();
        qmExp.exportQualityModelToXML(qualityModel, Paths.get(OUTPUT.toString(), "qm_output"));

        System.out.println("...");
    }

    /**
     * Initialize results directory and handle input parameters.
     * This helper method avoids ordering problems of input params but assigning
     * the values to a key-value hashmap
     *
     * @param inputArgs project and results location as described in main method
     * @return HashMap
     */
    private static HashMap<String, String> initialize(String[] inputArgs) {

        HashMap<String, String> config = new HashMap<>();

        if (inputArgs[0].equalsIgnoreCase("true") || inputArgs[0].equalsIgnoreCase("false")) {
            config.put("recalibrate", inputArgs[0]);
        }
        else throw new RuntimeException("inputArgs[0] did not match 'true' or 'false'");

        if (inputArgs[1].equalsIgnoreCase("true") || inputArgs[1].equalsIgnoreCase("false")) {
            config.put("rerunTools", inputArgs[1]);
        }
        else throw new RuntimeException("inputArgs[1] did not match 'true' or 'false'");

        config.put("benchRepoPath", inputArgs[2]);
        return config;
    }

}
