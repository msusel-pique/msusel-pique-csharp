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
import qatch.csharp.runnable.SingleProjectEvaluation;

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
    public void testSingleProjectEvaluation() throws FileNotFoundException {
        final Path PROJECT_PATH = Paths.get("src/test/resources/TestCsharpProject");
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

    private void cleanTestOutput() {
        try {
            FileUtils.deleteDirectory(TEST_OUT.toFile());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

//    @Test
//    public void testSingleProjectEvaluation() throws SAXException, ParserConfigurationException, CloneNotSupportedException, IOException {
//
//        Path projectPath = Paths.get("C:\\Users\\davidrice3\\Desktop\\temp\\Eleflex\\src\\Applications\\Eleflex.WebClient");
//        String resultPath = "./src/test/output";
//
//        clean(new File(resultPath));
//        SingleProjectEvaluation.main(new String[]{projectPath.toString(), resultPath});
//
//        File evalResults = new File(resultPath + "/qa-results/" + projectPath.getFileName() + "_evalResults.json");
//
//        Assert.assertTrue(evalResults.exists());
//
//        JsonParser parser = new JsonParser();
//        JsonObject data = (JsonObject) parser.parse(new FileReader(evalResults));
//        double eval = data.getAsJsonObject("tqi").get("eval").getAsDouble();
//
//        Assert.assertTrue(eval < 0.9999 && eval > 0.0001);
//    }

    private void clean(File toClean) throws IOException {
        if (toClean.exists()) {
            FileUtils.cleanDirectory(toClean);
        }
        else toClean.mkdirs();
    }

}
