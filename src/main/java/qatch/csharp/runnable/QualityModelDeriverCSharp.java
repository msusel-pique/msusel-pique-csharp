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
    private static final Path ROSLYN_RESOURCE_ROOT = Paths.get(RESOURCES.toString(), "Roslynator");

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
        IToolLOC loc = new RoslynatorLoc(ROSLYN_RESOURCE_ROOT, Paths.get(properties.getProperty("msbuild.bin")));
        ITool roslynator = new RoslynatorAnalyzer(ROSLYN_RESOURCE_ROOT, Paths.get(properties.getProperty("msbuild.bin")));
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

}
