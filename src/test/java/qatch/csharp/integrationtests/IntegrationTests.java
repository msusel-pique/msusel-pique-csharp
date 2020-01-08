package qatch.csharp.integrationtests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import qatch.analysis.Diagnostic;
import qatch.csharp.RoslynatorAnalyzer;
import qatch.csharp.TestHelper;
import qatch.csharp.runnable.QualityModelDeriverCSharp;
import qatch.csharp.runnable.SingleProjectEvaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

@Category(IntegrationTest.class)
public class IntegrationTests {

    private final Path ROSLYN_ROOT = Paths.get("src/main/resources/Roslynator");
    private final Path TARGET = Paths.get("src/test/resources/projects/TestNetFramework");

    @Before
    public void setUp() throws Exception {
        TestHelper.cleanTestOutput();
    }

    @Test
    public void testQualityModelDeriver() throws IOException {

        Path qmDeriverConfig = Paths.get("src/test/resources/config/quality_model_deriver.properties");
        Properties properties = new Properties();
        try { properties.load((new FileInputStream(qmDeriverConfig.toString()))); }
        catch (IOException e) { e.printStackTrace(); }

        QualityModelDeriverCSharp.main(new String[] { qmDeriverConfig.toString() });

        // Parse resulting QM file
        Path result = Paths.get(properties.getProperty("results.directory"), "qualityModel_CSharpRoslynatorTestQM.json");
        FileReader fr = new FileReader(result.toString());
        JsonObject jsonResults = new JsonParser().parse(fr).getAsJsonObject();
        fr.close();

        // Assert expected results
        JsonObject jsonTqi = jsonResults.getAsJsonObject("tqi");
        JsonObject jsonCharacteristics = jsonResults.getAsJsonObject("characteristics");
        JsonObject jsonChar01 = jsonCharacteristics.getAsJsonObject("Characteristic 01");
        JsonObject jsonChar02 = jsonCharacteristics.getAsJsonObject("Characteristic 02");
        JsonObject jsonProperties = jsonResults.getAsJsonObject("properties");
        JsonObject jsonProperty01 = jsonProperties.getAsJsonObject("Property 01");
        JsonObject jsonProperty02 = jsonProperties.getAsJsonObject("Property 02");

        Assert.assertEquals("CSharp Roslynator Test QM", jsonResults.getAsJsonPrimitive("name").getAsString());

        Assert.assertEquals("TQI", jsonTqi.getAsJsonPrimitive("name").getAsString());
        Assert.assertEquals(0.6667, jsonTqi.getAsJsonObject("weights").getAsJsonPrimitive("Characteristic 01").getAsDouble(), 0.0001);
        Assert.assertEquals(0.3333, jsonTqi.getAsJsonObject("weights").getAsJsonPrimitive("Characteristic 02").getAsDouble(), 0.0001);

        Assert.assertEquals(0.25, jsonChar01.getAsJsonObject("weights").getAsJsonPrimitive("Property 01").getAsDouble(), 0.0001);
        Assert.assertEquals(0.75, jsonChar01.getAsJsonObject("weights").getAsJsonPrimitive("Property 02").getAsDouble(), 0.0001);

        Assert.assertEquals(0.8, jsonChar02.getAsJsonObject("weights").getAsJsonPrimitive("Property 01").getAsDouble(), 0.0001);
        Assert.assertEquals(0.2, jsonChar02.getAsJsonObject("weights").getAsJsonPrimitive("Property 02").getAsDouble(), 0.0001);

        Assert.assertEquals(0.0769, jsonProperty01.getAsJsonArray("thresholds").get(0).getAsFloat(), 0.0001);
        Assert.assertEquals(0.1333, jsonProperty01.getAsJsonArray("thresholds").get(1).getAsFloat(), 0.0001);
        Assert.assertEquals(0.1951, jsonProperty01.getAsJsonArray("thresholds").get(2).getAsFloat(), 0.0001);

        Assert.assertEquals(0.0, jsonProperty02.getAsJsonArray("thresholds").get(0).getAsFloat(), 0.0001);
        Assert.assertEquals(0.00, jsonProperty02.getAsJsonArray("thresholds").get(1).getAsFloat(), 0.0001);
        Assert.assertEquals(0.0732, jsonProperty02.getAsJsonArray("thresholds").get(2).getAsFloat(), 0.0001);

    }

    /**
     * Test entire analysis module procedure using Roslynator:
     *   (1) run Roslynator tool
     *   (2) parse: get object representation of the diagnostics described by the QM
     *   (3) make collection of diagnostic objects
     */
    @Test
    public void testRoslynatorAnalysis() throws IOException {

        // Initialize main objects
        Properties properties = new Properties();
        properties.load((new FileInputStream("src/test/resources/config/single_project_evaluation.properties")));

        RoslynatorAnalyzer roslynatorAnalyzer = new RoslynatorAnalyzer(ROSLYN_ROOT, Paths.get(properties.getProperty("msbuild.bin")));

        // (1) run Roslynator tool
        Path analysisOutput = roslynatorAnalyzer.analyze(TARGET);

        // (2 and 3) parse: get object representation of the diagnostics described by the QM
        Map<String, Diagnostic> analysisResults = roslynatorAnalyzer.parseAnalysis(analysisOutput);

        // Assert the results has the finidngs from the tool analysis scan
        Assert.assertEquals(3, analysisResults.size());
        Assert.assertEquals(2, analysisResults.get("RCS1018").getFindings().size());
        Assert.assertEquals(1, analysisResults.get("RCS1163").getFindings().size());
        Assert.assertEquals(1, analysisResults.get("SCS0005").getFindings().size());
    }

    /**
     * Test entire evaluation process on a C# project or solution.
     */
    @Test
    public void testSingleProjectEvaluation() throws IOException {

        // Initialize config
        Path singleProjectEvalConfig = Paths.get("src/test/resources/config/single_project_evaluation.properties");
        Properties properties = new Properties();
        try { properties.load((new FileInputStream(singleProjectEvalConfig.toString()))); }
        catch (IOException e) { e.printStackTrace(); }

        // Run evaluation
        SingleProjectEvaluation.main(new String[] { singleProjectEvalConfig.toString() });

        // Handle results
        String projectName = FilenameUtils.getBaseName(properties.getProperty("project.root"));
        File evalResults = new File(
                properties.getProperty("results.directory"),
                projectName + File.separator + projectName + "_evalResults.json"
        );
        FileReader fr = new FileReader(evalResults);
        JsonParser parser = new JsonParser();
        JsonObject data = (JsonObject) parser.parse(fr);
        fr.close();

        JsonObject additionalData = data.getAsJsonObject("additionalData");
        JsonObject jsonTqi = data.getAsJsonObject("tqi");
        JsonObject jsonCharacteristics = data.getAsJsonObject("characteristics");
        JsonObject jsonChar01 = jsonCharacteristics.getAsJsonObject("Characteristic 01");
        JsonObject jsonChar02 = jsonCharacteristics.getAsJsonObject("Characteristic 02");
        JsonObject jsonProperties = data.getAsJsonObject("properties");
        JsonObject jsonProperty01 = jsonProperties.getAsJsonObject("Property 01");
        JsonObject jsonProperty02 = jsonProperties.getAsJsonObject("Property 02");
        JsonObject jsonMeasure01 = jsonProperty01.getAsJsonObject("measure");
        JsonObject jsonMeasure02 = jsonProperty02.getAsJsonObject("measure");

        // Asserts
        Assert.assertEquals("TestNetFramework", additionalData.getAsJsonPrimitive("projectName").getAsString());
        Assert.assertEquals("39", additionalData.getAsJsonPrimitive("projectLinesOfCode").getAsString());

        Assert.assertEquals("Test Roslynator Analysis", data.getAsJsonPrimitive("name").getAsString());

        Assert.assertEquals("TQI", jsonTqi.getAsJsonPrimitive("name").getAsString());
        Assert.assertEquals(0.1212, jsonTqi.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.8, jsonTqi.getAsJsonObject("weights").getAsJsonPrimitive("Characteristic 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.2, jsonTqi.getAsJsonObject("weights").getAsJsonPrimitive("Characteristic 02").getAsDouble(), 0.001);

        Assert.assertEquals(.1153, jsonChar01.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.6, jsonChar01.getAsJsonObject("weights").getAsJsonPrimitive("Property 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.4, jsonChar01.getAsJsonObject("weights").getAsJsonPrimitive("Property 02").getAsDouble(), 0.001);

        Assert.assertEquals(0.1442, jsonChar02.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.5, jsonChar02.getAsJsonObject("weights").getAsJsonPrimitive("Property 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.5, jsonChar02.getAsJsonObject("weights").getAsJsonPrimitive("Property 02").getAsDouble(), 0.001);

        Assert.assertEquals(0.0, jsonProperty01.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.0, jsonProperty01.getAsJsonArray("thresholds").get(0).getAsFloat(), 0.001);
        Assert.assertEquals(0.04, jsonProperty01.getAsJsonArray("thresholds").get(1).getAsFloat(), 0.001);
        Assert.assertEquals(0.1, jsonProperty01.getAsJsonArray("thresholds").get(2).getAsFloat(), 0.001);

        Assert.assertEquals(0.2885, jsonProperty02.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.0, jsonProperty02.getAsJsonArray("thresholds").get(0).getAsFloat(), 0.001);
        Assert.assertEquals(0.06, jsonProperty02.getAsJsonArray("thresholds").get(1).getAsFloat(), 0.0001);
        Assert.assertEquals(0.1, jsonProperty02.getAsJsonArray("thresholds").get(2).getAsFloat(), 0.0001);

        Assert.assertEquals(0.1538, jsonMeasure01.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.0769, jsonMeasure02.getAsJsonPrimitive("value").getAsFloat(), 0.001);
    }
}
