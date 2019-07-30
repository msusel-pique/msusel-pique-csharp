package qatch.csharp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class IntegrationTests {

    @Test
    public void testSingleProjectEvaluation() throws SAXException, ParserConfigurationException, CloneNotSupportedException, IOException {

        String projectName = "FxcopFindings";
        String projectPath = "../../MSUSEL/sample-analysis-projects/csharp/" + projectName;
        String resultPath = "./src/test/output";

        clean(new File(resultPath));
        SingleProjectEvaluation.main(new String[]{projectPath, resultPath});

        File evalResults = new File(resultPath + "/qa-results/" + projectName + "_evalResults.json");

        Assert.assertTrue(evalResults.exists());

        JsonParser parser = new JsonParser();
        JsonObject data = (JsonObject) parser.parse(new FileReader(evalResults));
        double eval = data.getAsJsonObject("tqi").get("eval").getAsDouble();

        Assert.assertTrue(eval < 0.999 && eval > 0.001);
    }

    private void clean(File toClean) throws IOException {
        if (toClean.exists()) {
            FileUtils.cleanDirectory(toClean);
        }
        else toClean.mkdirs();
    }

}
