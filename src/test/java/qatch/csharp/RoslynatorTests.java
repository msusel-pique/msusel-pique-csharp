package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RoslynatorTests {

    @Before
    public void cleanBefore() throws IOException {
        FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/output"));
    }

    @After
    public void cleanAfter() throws IOException {
        FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/output"));
    }

    @Test
    public void testAnalyze() {

        Roslynator roslynator = new Roslynator(
                "Roslynator",
                Paths.get("src/test/resources/config/roslynator_test_measures.yaml"),
                Paths.get("src/main/resources/tools"),
                Paths.get("C:/Program Files (x86)/Microsoft Visual Studio/2017/Community/MSBuild/15.0/Bin")
        );
        Path target = Paths.get("src\\test\\resources\\net_framework_solution\\TestNetFramework\\TestNetFramework.sln");

        Path analysisOutput = roslynator.analyze(target);
        File result = analysisOutput.toFile();

        // XML file exists in expected location with correct name
        Assert.assertTrue(result.exists());
        Assert.assertTrue(result.isFile());
        Assert.assertEquals("roslynator_output.xml", result.getName());

        // XML file has expected number of bytes
        Assert.assertTrue(result.length() > 1001);
    }

}
