package pique.csharp.runnable;

import pique.analysis.ITool;
import pique.csharp.RoslynatorLoc;
import pique.csharp.RoslynatorAnalyzer;
import pique.model.QualityModel;
import pique.model.QualityModelExport;
import pique.model.QualityModelImport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelDeriver {

    // Fields

    private static final File RESOURCES = new File("src/main/resources");
    private static final Path ROSLYN_RESOURCE_ROOT = Paths.get(RESOURCES.toString(), "Roslynator");


    /**
     * Main method for deriving a C# quality model.
     * Elicitates weights and determines thresholds using a benchmark repository.
     * This method's main purpose is to assist configuration and then just call the
     * msusel-pique QualityModelDriver.deriveModel() method.
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
        String projectRootFlag = properties.getProperty("target.flag");
        Path qmFilePath = Paths.get(properties.getProperty("qm.filepath"));
        Path benchmarkRepository = Paths.get(properties.getProperty("benchmark.repo"));
        Path outputDirectory = Paths.get(properties.getProperty("results.directory"));
        ITool roslynatorLoc = new RoslynatorLoc(ROSLYN_RESOURCE_ROOT, Paths.get(properties.getProperty("msbuild.bin")));
        ITool roslynator = new RoslynatorAnalyzer(ROSLYN_RESOURCE_ROOT, Paths.get(properties.getProperty("msbuild.bin")));
        Set<ITool> tools = Stream.of(roslynatorLoc, roslynator).collect(Collectors.toSet());

        QualityModelImport qmImport = new QualityModelImport(qmFilePath);
        QualityModel qmDescription = qmImport.importQualityModel();

        // Run derivation process
        QualityModel derivedQM = pique.runnable.QualityModelDeriver.deriveModel(
                qmDescription, tools, benchmarkRepository,  projectRootFlag);

        // Output to file
        Path jsonOutput = new QualityModelExport(derivedQM).exportToJson(derivedQM.getName(), outputDirectory);

        System.out.println("Quality Model derivation finished. You can find the file at " + jsonOutput.toAbsolutePath().toString());
    }

}
