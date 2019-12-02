package qatch.csharp.runnable;

import qatch.analysis.ITool;
import qatch.analysis.IToolLOC;
import qatch.csharp.RoslynatorLoc;
import qatch.csharp.RoslynatorAnalyzer;
import qatch.model.QualityModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

public class QualityModelDeriverCSharp {

    // Fields
    private static final File RESOURCES = new File("src/main/resources");
    private static final File TOOLS = new File(RESOURCES, "tools");

    /**
     * Main method for deriving a C# quality model.
     * Elicites weights using an AHP matrix and determines thresholds using a benchmark repository.
     * This method's main purpose is to assist configuration and then just call the
     * msusel-qatch QualityModelDriver.deriveModel() method.
     *
     * @param args configuration array:
     *        0: path to config file. See the quality_model_deriver.properties file in src/test/resources/config for an example.
     */
    public static void main(String[] args) {

        // Setup
        Properties properties = new Properties();
        try { properties.load((new FileInputStream(args[0]))); }
        catch (IOException e) { e.printStackTrace(); }

        // Initialize inputs
        QualityModel qmDescription = new QualityModel(Paths.get(properties.getProperty("qm.filepath")));
        IToolLOC loc = new RoslynatorLoc(TOOLS.toPath(), Paths.get(properties.getProperty("msbuild.bin")));
        ITool roslynator = new RoslynatorAnalyzer(TOOLS.toPath(), Paths.get(properties.getProperty("msbuild.bin")));
        HashMap<String, ITool> tools = new HashMap<String, ITool>() {{ put(roslynator.getName(), roslynator); }};
        Path benchmarkRepository = Paths.get(properties.getProperty("benchmark.repo"));
        Path comparisonMatricesDirectory = Paths.get(properties.getProperty("comparison.matrices"));
        Path benchmarkScanOutput = Paths.get(properties.getProperty("benchmarkscan.output"));
        Path rThresholdsOutput = Paths.get(properties.getProperty("rthresholds.output"));
        Path tempWeightsDirectory = Paths.get(properties.getProperty("rweights.output"));
        String projectRootFlag = properties.getProperty("target.flag");
        Path outputDirectory = Paths.get(properties.getProperty("results.directory"));

        // Run derivation process
        QualityModel derivedQM = qatch.runnable.QualityModelDeriver.deriveModel(
                qmDescription, loc, tools, benchmarkRepository, comparisonMatricesDirectory,
                benchmarkScanOutput, rThresholdsOutput, tempWeightsDirectory, projectRootFlag
        );

        // Output to file
        Path jsonOutput = derivedQM.exportToJson(outputDirectory);

        System.out.println("Quality Model derivation finished. You can find the file at " + jsonOutput.toAbsolutePath().toString());
    }

    /**
     * Initialize results directory and handle input parameters.
     * This helper method avoids ordering problems of input params by assigning
     * the values to a key-value hashmap
     *
     * @param inputArgs project and results location as described in main method
     * @return HashMap of config with following key strings:
     *      [qmPath, benchmarkRepository, comparisonMatrices, benchmarkData, thresholdOut, weightsOut, flag, output]
     */
    private static HashMap<String, String> initialize(String[] inputArgs) {

        HashMap<String, String> config = new HashMap<>();

        if (inputArgs.length != 8) {
            throw new RuntimeException("inputArgs should be of length 8");
        }

        if (inputArgs[0].endsWith(".json")) {
            config.put("qmPath", inputArgs[0]);
        }
        else throw new RuntimeException("inputArgs[0] (quality model path) does not end with '.json'");

        if (Paths.get(inputArgs[1]).toFile().isDirectory()) {
            config.put("benchmarkRepository", inputArgs[1]);
        }
        else throw new RuntimeException("inputArgs[1] (benchmark repository output) is not a path to a directory");

        if (Paths.get(inputArgs[2]).toFile().isDirectory()) {
            config.put("comparisonMatrices", inputArgs[2]);
        }
        else throw new RuntimeException("inputArgs[2] (comparison matrices output) is not a path to a directory");

        if (inputArgs[3].endsWith(".csv")) {
            config.put("benchmarkData", inputArgs[3]);
        }
        else throw new RuntimeException("inputArgs[3] (benchmark data output) does not end with '.csv'");

        Paths.get(inputArgs[4]).toFile().mkdirs();
        if (Paths.get(inputArgs[4]).toFile().isDirectory()) {
            config.put("thresholdOut", inputArgs[4]);
        }
        else throw new RuntimeException("inputArgs[4] (threshold output) is not a path to a directory");

        Paths.get(inputArgs[5]).toFile().mkdirs();
        if (Paths.get(inputArgs[5]).toFile().isDirectory()) {
            config.put("weightsOut", inputArgs[5]);
        }
        else throw new RuntimeException("inputArgs[5] (weights output) is not a path to a directory");

        config.put("flag", inputArgs[6]);

        Paths.get(inputArgs[7]).toFile().mkdirs();
        if (Paths.get(inputArgs[7]).toFile().isDirectory()) {
            config.put("output", inputArgs[7]);
        }
        else throw new RuntimeException("inputArgs[7] (output directory) is not a path to a directory");

        return config;
    }

}
