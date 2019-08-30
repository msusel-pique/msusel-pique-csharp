package qatch.csharp.integrationtests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;
import qatch.csharp.runnable.QualityModelGenerator;
import qatch.csharp.runnable.SingleProjectEvaluation;
import qatch.csharp.runnable.SolutionEvaluation;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Category(IntegrationTest.class)
public class IntegrationTests {

    private Path TEST_OUT = Paths.get("src/test/output");

    @Before
    public void cleanBefore() {
        cleanTestOutput();
    }

    @After
    public void cleanAfter()  {
        cleanTestOutput();
    }


    @Test
    public void testQualityModelGenerator() {
        final String CALIBRATE = "true";
        final String RUN_TOOLS = "true";
        final String REPO_PATH = "src/test/resources/multi_project_eval";
        final String OUT = "src/test/output";

        QualityModelGenerator.main(new String[] { CALIBRATE, RUN_TOOLS, REPO_PATH, OUT });

        // TODO: add assertion checks

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
        File charlieResults = new File(qa_results, "Charlie" + File.separator + "Charlie_evalResults.json");

        JsonParser parser = new JsonParser();
        JsonObject alphaData = (JsonObject) parser.parse(new FileReader(alphaResults));
        JsonObject bravoData = (JsonObject) parser.parse(new FileReader(bravoResults));
        JsonObject charlieData = (JsonObject) parser.parse(new FileReader(charlieResults));

        double alphaEval = alphaData.getAsJsonObject("tqi").get("eval").getAsDouble();
        double bravoEval = bravoData.getAsJsonObject("tqi").get("eval").getAsDouble();
        double charlieEval = charlieData.getAsJsonObject("tqi").get("eval").getAsDouble();

        Assert.assertTrue(alphaResults.exists());
        Assert.assertTrue(bravoResults.exists());
        Assert.assertTrue(charlieResults.exists());
        Assert.assertTrue(alphaEval < 0.9999 && alphaEval > 0.0001);
        Assert.assertTrue(bravoEval < 0.9999 && bravoEval > 0.0001);
        Assert.assertTrue(charlieEval < 0.9999 && charlieEval > 0.0001);
    }


    private void cleanTestOutput() {
        try {
            FileUtils.deleteDirectory(TEST_OUT.toFile());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
