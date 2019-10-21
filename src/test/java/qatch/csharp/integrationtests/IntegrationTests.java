package qatch.csharp.integrationtests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import qatch.csharp.Roslynator;
import qatch.csharp.TestHelper;
import qatch.csharp.runnable.QualityModelGenerator;
import qatch.csharp.runnable.SingleProjectEvaluation;
import qatch.csharp.runnable.SolutionEvaluation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Category(IntegrationTest.class)
public class IntegrationTests {

    final Path TEST_OUT = Paths.get("src/test/output");
    final String ROSLYN_NAME = "Roslynator",
                 CONFIG_LOC  = "src/test/resources/config/roslynator_test_measures.yaml",
                 TOOLS_LOC   = "src/main/resources/tools",
                 TARGET_LOC  = "src/test/resources/net_framework_solution/TestNetFramework/TestNetFramework.sln";

    @Test
    public void testQualityModelGenerator() {
        final String CALIBRATE = "true";
        final String RUN_TOOLS = "true";
        final String REPO_PATH = "src/test/resources/multi_project_eval";
        final String OUT = "src/test/output";

        QualityModelGenerator.main(new String[] { CALIBRATE, RUN_TOOLS, REPO_PATH, OUT });

        // TODO: add assertion checks

    }


    /**
     * Test entire analysis module procedure using Roslynator:
     *   (1) run Roslynator tool
     *   (2) prase output, apply findings and diagnostics to Measure objects
     *   (3) link Measure objects to properties using .yaml measure mapping config
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



        System.out.println("...");
    }


    @Test
    public void testSingleProjectEvaluation() throws FileNotFoundException {
        final Path PROJECT_PATH = Paths.get("src/test/resources/single_project_eval/TestCsharpProject");
        final Path RESULT_PATH = TEST_OUT;

        SingleProjectEvaluation.main(new String[] { PROJECT_PATH.toString(), RESULT_PATH.toString() });

        File evalResults = new File(
                RESULT_PATH.toFile(),
                PROJECT_PATH.getFileName().toString() + File.separator + PROJECT_PATH.getFileName().toString() + "_evalResults.json"
        );
        JsonParser parser = new JsonParser();
        JsonObject data = (JsonObject) parser.parse(new FileReader(evalResults));
        double eval = data.getAsJsonObject("tqi").get("eval").getAsDouble();

        Assert.assertTrue(evalResults.exists());
        Assert.assertTrue(eval < 0.9999 && eval > 0.0001);
    }


    @Test
    public void testSolutionEvaluation() throws FileNotFoundException {
        final Path SOLUTION_PATH = Paths.get("src/test/resources/multi_project_eval");
        final Path RESULT_PATH = TEST_OUT;

        SolutionEvaluation.main(new String[] { SOLUTION_PATH.toString(), RESULT_PATH.toString() });

        File qa_results = new File(RESULT_PATH.toFile(), "qa_out");
        File alphaResults = new File(qa_results, "Alpha" + File.separator + "Alpha_evalResults.json");
        File bravoResults = new File(qa_results, "Bravo" + File.separator + "Bravo_evalResults.json");

        JsonParser parser = new JsonParser();
        JsonObject alphaData = (JsonObject) parser.parse(new FileReader(alphaResults));
        JsonObject bravoData = (JsonObject) parser.parse(new FileReader(bravoResults));

        double alphaEval = alphaData.getAsJsonObject("tqi").get("eval").getAsDouble();
        double bravoEval = bravoData.getAsJsonObject("tqi").get("eval").getAsDouble();

        Assert.assertTrue(alphaResults.exists());
        Assert.assertTrue(bravoResults.exists());
        Assert.assertTrue(alphaEval < 0.9999 && alphaEval > 0.0001);
        Assert.assertTrue(bravoEval < 0.9999 && bravoEval > 0.0001);
    }
}
