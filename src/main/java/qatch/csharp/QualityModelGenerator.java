package qatch.csharp;

import qatch.analysis.IAnalyzer;
import qatch.calibration.BenchmarkAnalysisExporter;
import qatch.calibration.BenchmarkAnalyzer;
import qatch.calibration.BenchmarkProjects;
import qatch.model.CharacteristicSet;
import qatch.model.PropertySet;
import qatch.model.QualityModel;
import qatch.model.Tqi;

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
     *             1: path of the benchmark repository directory. Can be relative or full path
     */
    public static void main(String[] args) {

        // constants
        final boolean BENCHMARK_CALIBRATION;
        final Path BENCH_REPO_PATH;
        final Path OUTPUT = new File("./out").toPath();
        final String PROJ_ROOT_FLAG = ".csproj";    // identifies individual C# project roots in the repo (at any depth)

        System.out.println("\n\n******************************  Model Generator *******************************");
        System.out.println("*");

        // Initialize
        if (args == null || args.length < 1) {
            throw new RuntimeException("Incorrect input parameters given. Be sure to include \n\t(1) [true | false] "
                + "flag for benchmark calibration.\n\t(2) location of benchmark repository");
        }
        HashMap<String, String> config = initialize(args);

        BENCHMARK_CALIBRATION = Boolean.parseBoolean(config.get("benchmarkCalibration"));
        BENCH_REPO_PATH = Paths.get(config.get("benchRepoPath"));
        OUTPUT.toFile().mkdirs();

        System.out.println("* Starting Analysis...");
        System.out.println("* Loading Quality Model...");

        // Set the properties and the characteristics
        QualityModel qualityModel = new QualityModel();
        PropertySet properties = qualityModel.getProperties();
        CharacteristicSet characteristics = qualityModel.getCharacteristics();
        Tqi tqi = qualityModel.getTqi();

        System.out.println("* Empty Quality Model successfully loaded...!");
        System.out.println("*");

        BenchmarkProjects projects = null;
        BenchmarkAnalysisExporter exporter = null;

        // Check if the user wants to execute a benchmark calibration
        if (BENCHMARK_CALIBRATION) {
            /*
             * Step 1 : Analyze the projects found in the desired Benchmark Repository
             */
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

        config.put("benchRepoPath", inputArgs[1]);

        return config;
    }

}
