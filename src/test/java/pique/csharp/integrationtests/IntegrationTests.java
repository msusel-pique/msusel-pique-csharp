package pique.csharp.integrationtests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import pique.csharp.RoslynatorAnalyzer;
import pique.csharp.RoslynatorLoc;
import pique.csharp.TestHelper;
import pique.csharp.runnable.ModelDeriver;
import pique.csharp.runnable.Assessment;
import pique.model.Diagnostic;
import pique.model.Finding;

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

        ModelDeriver.main(new String[] { qmDeriverConfig.toString() });

        // Parse resulting QM file
        Path result = Paths.get(properties.getProperty("results.directory"), "qualityModel_TestRoslynatorAnalysis.json");
        FileReader fr = new FileReader(result.toString());
        JsonObject data = new JsonParser().parse(fr).getAsJsonObject();
        fr.close();

        // Assert expected results
        String qmName = data.getAsJsonPrimitive("name").getAsString();
        JsonObject factors = data.getAsJsonObject("factors");

        JsonObject tqi = factors.getAsJsonObject("tqi").getAsJsonObject("TQI");

        JsonObject qualityAspects = factors.getAsJsonObject("quality_aspects");
        JsonObject qualiyAspect01 = qualityAspects.getAsJsonObject("QualityAspect 01");
        JsonObject qualiyAspect02 = qualityAspects.getAsJsonObject("QualityAspect 02");

        JsonObject measure = data.getAsJsonObject("measures");
        JsonObject measure01 = measure.getAsJsonObject("Measure 01");

        // Assert
        Assert.assertEquals("Test Roslynator Analysis", qmName);

        Assert.assertEquals(0.5, tqi.getAsJsonObject("weights").getAsJsonPrimitive("QualityAspect 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.5, tqi.getAsJsonObject("weights").getAsJsonPrimitive("QualityAspect 02").getAsDouble(), 0.001);

        Assert.assertEquals(0.5, qualiyAspect01.getAsJsonObject("weights").getAsJsonPrimitive("ProductFactor 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.5, qualiyAspect01.getAsJsonObject("weights").getAsJsonPrimitive("ProductFactor 02").getAsDouble(), 0.001);

        Assert.assertEquals(0.5, qualiyAspect02.getAsJsonObject("weights").getAsJsonPrimitive("ProductFactor 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.5, qualiyAspect02.getAsJsonObject("weights").getAsJsonPrimitive("ProductFactor 02").getAsDouble(), 0.001);

        Assert.assertEquals(0.0769, measure01.getAsJsonArray("thresholds").get(0).getAsFloat(), 0.001);
        Assert.assertEquals(0.1951, measure01.getAsJsonArray("thresholds").get(1).getAsFloat(), 0.0001);

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

    @Test
    public void testRoslynatorLoCAnalysis() throws IOException {

        // Initialize main objects
        Properties properties = new Properties();
        properties.load((new FileInputStream("src/test/resources/config/single_project_evaluation.properties")));

        RoslynatorLoc roslynatorLoc = new RoslynatorLoc(ROSLYN_ROOT, Paths.get(properties.getProperty("msbuild.bin")));

        Path locOutput = roslynatorLoc.analyze(TARGET);

        Map<String, Diagnostic> analysisResults = roslynatorLoc.parseAnalysis(locOutput);

        Finding locFinding = analysisResults.get("loc").getFindings().stream().findFirst().get();
        double loc = locFinding.getValue();
        Assert.assertEquals(39.0, loc, 0.0);
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
        Assessment.main(new String[] { singleProjectEvalConfig.toString() });

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

        String qmName = data.getAsJsonPrimitive("name").getAsString();
        JsonObject additionalData = data.getAsJsonObject("additionalData");
        JsonObject factors = data.getAsJsonObject("factors");

        JsonObject tqi = factors.getAsJsonObject("tqi").getAsJsonObject("TQI");

        JsonObject qualityAspects = factors.getAsJsonObject("quality_aspects");
        JsonObject qualiyAspect01 = qualityAspects.getAsJsonObject("QualityAspect 01");
        JsonObject qualiyAspect02 = qualityAspects.getAsJsonObject("QualityAspect 02");

        JsonObject productFactors = factors.getAsJsonObject("product_factors");
        JsonObject productFactor01 = productFactors.getAsJsonObject("ProductFactor 01");
        JsonObject productFactor02 = productFactors.getAsJsonObject("ProductFactor 02");

        JsonObject measure = data.getAsJsonObject("measures");
        JsonObject measure01 = measure.getAsJsonObject("Measure 01");
        JsonObject measure02 = measure.getAsJsonObject("Measure 02");

        // Asserts
        Assert.assertEquals("Test Roslynator Analysis", qmName);
        Assert.assertEquals("TestNetFramework", additionalData.getAsJsonPrimitive("projectName").getAsString());
        Assert.assertEquals("39", additionalData.getAsJsonPrimitive("projectLinesOfCode").getAsString());

        Assert.assertEquals(0.0323, tqi.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.8, tqi.getAsJsonObject("weights").getAsJsonPrimitive("QualityAspect 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.2, tqi.getAsJsonObject("weights").getAsJsonPrimitive("QualityAspect 02").getAsDouble(), 0.001);

        Assert.assertEquals(.0307, qualiyAspect01.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.6, qualiyAspect01.getAsJsonObject("weights").getAsJsonPrimitive("ProductFactor 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.4, qualiyAspect01.getAsJsonObject("weights").getAsJsonPrimitive("ProductFactor 02").getAsDouble(), 0.001);

        Assert.assertEquals(0.0384, qualiyAspect02.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.5, qualiyAspect02.getAsJsonObject("weights").getAsJsonPrimitive("ProductFactor 01").getAsDouble(), 0.001);
        Assert.assertEquals(0.5, qualiyAspect02.getAsJsonObject("weights").getAsJsonPrimitive("ProductFactor 02").getAsDouble(), 0.001);

        Assert.assertEquals(0.0, productFactor01.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.0769, productFactor02.getAsJsonPrimitive("value").getAsFloat(), 0.001);

        Assert.assertEquals(0.0, measure01.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.0, measure01.getAsJsonArray("thresholds").get(0).getAsFloat(), 0.001);
        Assert.assertEquals(0.1, measure01.getAsJsonArray("thresholds").get(1).getAsFloat(), 0.0001);

        Assert.assertEquals(0.0769, measure02.getAsJsonPrimitive("value").getAsFloat(), 0.001);
        Assert.assertEquals(0.0, measure02.getAsJsonArray("thresholds").get(0).getAsFloat(), 0.001);
        Assert.assertEquals(0.1, measure02.getAsJsonArray("thresholds").get(1).getAsFloat(), 0.0001);
    }
}
