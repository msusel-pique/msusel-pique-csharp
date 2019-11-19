package qatch.csharp.integrationtests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import qatch.analysis.Diagnostic;
import qatch.analysis.Measure;
import qatch.csharp.Roslynator;
import qatch.csharp.runnable.QualityModelDeriver;
import qatch.csharp.runnable.SingleProjectEvaluation;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

@Category(IntegrationTest.class)
public class IntegrationTests {

    private final Path TEST_OUT = Paths.get("src/test/out");
    private final String ROSLYN_NAME = "Roslynator",
                         CONFIG_LOC  = "src/test/resources/config/roslynator_test_measures.yaml",
                         TOOLS_LOC   = "src/main/resources/tools",
                         TARGET_LOC  = "src/test/resources/projects/TestNetFramework/TestNetFramework.sln";

    @Test
    public void testQualityModelDeriver() {

        /*
         * @param args configuration array in following order:
         *             0: Path to quality model description file
         *             1: Path to benchmark repository directory
         *             2: Path to comparison matrices directory
         *             3: Temporary path to file to fill with tool scan analysis results. This should be a filename
         *                path such as a/b/benchmark_data.csv.  Does not need to exist.
         *             4: Temporary path to desired directory to hold the output of running the R Thresholds script.
         *             5: Temporary path to directory to store the weights.json file generated by R analysis.
         *             6: String flag, usually a file extension, that signals that a project to be analyzed is
         *                within the directory the flag was found in. For example, ".sln".
         */

        final Path qmPath = Paths.get("src/test/resources/quality_models/roslynator_scs_description.json");
        final Path benchmarkRepository = Paths.get("src/test/resources/projects/test_benchmark_repository");
        final Path comparisonMatrices = Paths.get("src/test/resources/comparison_matrices/test_qm_generator");
        final Path benchmarkData = Paths.get(TEST_OUT.toString(), "benchmark_data.csv");
        final Path thresholdOut = Paths.get("src/test/out");
        final Path weightsOut = Paths.get("src/test/out");
        final String flag =".sln";

        QualityModelDeriver.main(new String[] {
                qmPath.toString(), benchmarkRepository.toString(), comparisonMatrices.toString(),
                benchmarkData.toString(), thresholdOut.toString(), weightsOut.toString(), flag
        });

        // TODO PICKUP: finish model deriver test
        System.out.println("...");
    }


    /**
     * Test entire analysis module procedure using Roslynator:
     *   (1) run Roslynator static analysis tool
     *   (2) parse config: get object representation of the .yaml measure->diagnostics configuration
     *   (3) prase output: make collection of diagnostic objects
     *   (4) link findings and diagnostics to Measure objects
     *
     * A successful analysis results in the tool producing a measureMappings variable
     * with similar structure to the input .yaml config but with Measure objects, and those Measure
     * objects have the actual findings from the analysis run included as Finding objects.
     */
    @Test
    public void testRoslynatorAnalysis() throws IOException {

        Properties properties = new Properties();
        properties.load((new FileInputStream("src/test/resources/config/config.properties")));

        Roslynator roslynator = new Roslynator(
                ROSLYN_NAME,
                Paths.get(CONFIG_LOC),
                Paths.get(TOOLS_LOC),
                Paths.get(properties.getProperty("MSBUILD_BIN"))
        );
        Path target = Paths.get(TARGET_LOC);

        // (1) run Roslynator tool
        Path analysisOutput = roslynator.analyze(target);

        // (2) parse config: get object representation of the .yaml measure->diagnostics configuration
        Map<String, Measure> propertyMeasureMap = roslynator.parseConfig(roslynator.getConfig());

        // (3) prase output: make collection of diagnostic objects
        Map<String, Diagnostic> analysisResults = roslynator.parseAnalysis(analysisOutput);

        // (4) link findings and diagnostics to Measure objects
        propertyMeasureMap = roslynator.applyFindings(propertyMeasureMap, analysisResults);

        // Assert the measureMappings object has the finidngs from the tool analysis scan
        Map<String, Measure> results = propertyMeasureMap;
        Measure injectionMeasure = results.get("Injection");
        Measure cryptoMeasure = results.get("Cryptography");

        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.containsKey("Injection"));
        Assert.assertTrue(results.containsKey("Cryptography"));

        Assert.assertEquals("Roslynator", injectionMeasure.getToolName());
        Assert.assertEquals("Roslynator", cryptoMeasure.getToolName());

        Assert.assertEquals(3, injectionMeasure.getDiagnostics().size());

        Assert.assertEquals("RCS1018", injectionMeasure.getDiagnostics().get(0).getId());
        Assert.assertEquals("example_unfound_diagnostic_01", injectionMeasure.getDiagnostics().get(1).getId());
        Assert.assertEquals("example_unfound_diagnostic_02", injectionMeasure.getDiagnostics().get(2).getId());

        Assert.assertEquals(2, injectionMeasure.getDiagnostics().get(0).getFindings().size());
        Assert.assertEquals(0, injectionMeasure.getDiagnostics().get(1).getFindings().size());
    }


    /**
     * Test entire evaluation process on a C# project or solution.
     */
    @Test
    public void testSingleProjectEvaluation() throws IOException {
        final Path PROJECT_PATH = Paths.get("src/test/resources/projects/TestNetFramework/TestNetFramework.sln");
        final Path RESULT_PATH = TEST_OUT;

        // run evaluation
        SingleProjectEvaluation.main(new String[] { PROJECT_PATH.toString(), RESULT_PATH.toString() });

        // handle results
        String projectName = FilenameUtils.getBaseName(PROJECT_PATH.getFileName().toString());
        File evalResults = new File(
                RESULT_PATH.toFile(),
                projectName + File.separator + projectName + "_evalResults.json"
        );
        FileReader fr = new FileReader(evalResults);
        JsonParser parser = new JsonParser();
        JsonObject data = (JsonObject) parser.parse(fr);
        fr.close();

        int loc = data.getAsJsonPrimitive("linesOfCode").getAsInt();
        double tqiValue = data.getAsJsonObject("tqi").getAsJsonPrimitive("value").getAsDouble();
        String tqiName = data.getAsJsonObject("tqi").getAsJsonPrimitive("name").getAsString();

        Assert.assertTrue(evalResults.exists());
        Assert.assertEquals(39, loc);
        Assert.assertEquals(0.58, tqiValue, 0.0001);
        Assert.assertEquals("Security", tqiName);
    }

}
