package qatch.csharp;

import org.apache.commons.io.FileUtils;
import qatch.analysis.IAnalyzer;
import qatch.calibration.BenchmarkAnalysisExporter;
import qatch.calibration.BenchmarkAnalyzer;
import qatch.calibration.BenchmarkProjects;
import qatch.calibration.BenchmarkResultImporter;
import qatch.model.*;

import java.io.File;
import java.io.IOException;
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
        final boolean BENCHMARK_CALIBRATION;
        final boolean RERUN_TOOLS;
        final Path BENCH_REPO_PATH;
        final Path OUTPUT = new File("./out").toPath();
        final Path QM_DESCRIPTION_PATH = Paths.get("C:\\Users\\davidrice3\\Repository\\msusel-qatch\\msusel-qatch-csharp\\src\\main\\resources\\models\\qualityModel_csharp_description.xml");
        final String PROJ_ROOT_FLAG = ".csproj";    // identifies individual C# project (module) roots in the repo (at any depth)


        System.out.println("\n\n******************************  Model Generator *******************************");
        System.out.println("*");

        // Initialize
        if (args == null || args.length < 2) {
            throw new RuntimeException("Incorrect input parameters given. Be sure to include " +
                "\n\t(0) [true | false] flag for benchmark calibration," +
                "\n\t(1) [true | false] flag for rerunning of tool analysis," +
                "\n\t(2) location of benchmark repository");
        }
        HashMap<String, String> config = initialize(args);

        BENCHMARK_CALIBRATION = Boolean.parseBoolean(config.get("benchmarkCalibration"));
        RERUN_TOOLS = Boolean.parseBoolean(config.get("rerunTools"));
        BENCH_REPO_PATH = Paths.get(config.get("benchRepoPath"));
        OUTPUT.toFile().mkdirs();

        System.out.println("* Benchmark Repository: " + BENCH_REPO_PATH.toString());
        System.out.println("* Output Directory: " + OUTPUT.toString());
        System.out.println("*\n");
        System.out.println("* Starting Analysis...");
        System.out.println("* Loading Quality Model...");

        // Set the properties and the characteristics
        QualityModel qualityModel = new PropertiesAndCharacteristicsLoader(QM_DESCRIPTION_PATH.toAbsolutePath().toString()).importQualityModel();
        PropertySet properties = qualityModel.getProperties();
        CharacteristicSet characteristics = qualityModel.getCharacteristics();
        Tqi tqi = qualityModel.getTqi();

        System.out.println("* Empty Quality Model successfully loaded...!");
        System.out.println("***");

        BenchmarkProjects projects = null;
        BenchmarkAnalysisExporter exporter = null;

        // Check if the user wants to execute a benchmark calibration
        if (BENCHMARK_CALIBRATION) {

            /*
             * Step 1 : Analyze the projects found in the desired Benchmark Repository
             */
            if (RERUN_TOOLS) {
                System.out.println("\n**************** STEP 1 : Benchmark Analyzer *************************");
                System.out.println("*");
                System.out.println("* Analyzing the projects of the desired benchmark repository");
                System.out.println("* Please wait...");
                System.out.println("*");

                //Instantiate the benchmark analyzer
                BenchmarkAnalyzer benchmarkAnal = new BenchmarkAnalyzer(properties, BENCH_REPO_PATH, OUTPUT);

                // actualize language-specific analyzers
                IAnalyzer metricsAnalyzer = new LOCMetricsAnalyzer();
                IAnalyzer findingsAnalyzer = new FxcopAnalyzer();

                //Start the analysis of the benchmark repository
                benchmarkAnal.analyzeBenchmarkRepo(metricsAnalyzer, findingsAnalyzer, PROJ_ROOT_FLAG);

                System.out.println("* You can find the results at : " + benchmarkAnal.getBENCH_RESULTS_PATH().toString());
                System.out.println("***");
                System.out.println();
            }
            else {
                System.out.println("\n**************** STEP 1 : Benchmark Analyzer *************************");
                System.out.println("*");
                System.out.println("* Skipping tool static analysis. Assuming results already exist.");
                System.out.println("*");
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
//            projects = benchmarkImporter.importResults(BenchmarkAnalyzer.BENCH_RESULT_PATH);

            // Print some informative messages to the console
            System.out.println("*");
            System.out.println("* The results are successfully imported..! ");
            System.out.println("***");
        }
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
            config.put("benchmarkCalibration", inputArgs[0]);
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
