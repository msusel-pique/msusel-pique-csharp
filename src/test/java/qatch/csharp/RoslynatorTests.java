package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import qatch.analysis.Diagnostic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class RoslynatorTests {

    final String ROSLYN_NAME = "Roslynator",
                 CONFIG_LOC  = "src/test/resources/config/roslynator_test_measures.yaml",
                 TOOLS_LOC   = "src/main/resources/tools",
                 TARGET_LOC  = "src/test/resources/net_framework_solution/TestNetFramework/TestNetFramework.sln",
                 OUTPUT_LOC  = System.getProperty("user.dir") + "/output",
                 SAMPLE_OUTPUT_LOC = "src/test/resources/roslynator_output.xml";

    @Before
    public void cleanBefore() throws IOException {
        FileUtils.deleteDirectory(new File(OUTPUT_LOC));
    }

    @After
    public void cleanAfter() throws IOException {
        FileUtils.deleteDirectory(new File(OUTPUT_LOC));
    }

    @Test
    public void testAnalyze() throws IOException {

        Properties properties = new Properties();
        // TODO (maybe): Find source control friendly way to deal with MSBuild location property.
        properties.load((new FileInputStream("src/test/resources/config/config.properties")));

        Roslynator roslynator = new Roslynator(
                ROSLYN_NAME,
                Paths.get(CONFIG_LOC),
                Paths.get(TOOLS_LOC),
                Paths.get(properties.getProperty("MSBUILD_BIN"))
        );
        Path target = Paths.get(TARGET_LOC);

        Path analysisOutput = roslynator.analyze(target);
        File result = analysisOutput.toFile();

        // XML file exists in expected location with correct name
        Assert.assertTrue("Check that MSBUILD_BIN in config.properties points to a valid MSBuild bin location", result.exists());
        Assert.assertTrue(result.isFile());
        Assert.assertEquals("roslynator_output.xml", result.getName());

        // XML file has expected number of bytes
        Assert.assertTrue(result.length() > 1001);
    }

    @Test
    public void testParse() {

        Roslynator roslynator = new Roslynator(
                ROSLYN_NAME,
                Paths.get(CONFIG_LOC),
                null,
                null
        );
        Map<String, Diagnostic> diagnosticMap = roslynator.parseAnalysis(Paths.get(SAMPLE_OUTPUT_LOC));

        Diagnostic rcs1018 = diagnosticMap.get("RCS1018");
        Diagnostic rcs1102 = diagnosticMap.get("RCS1102");
        Diagnostic rcs1163 = diagnosticMap.get("RCS1163");

        Assert.assertEquals(3, diagnosticMap.size());

        Assert.assertEquals(2, rcs1018.getFindings().size());
        Assert.assertEquals(1, rcs1102.getFindings().size());
        Assert.assertEquals(1, rcs1163.getFindings().size());

        Assert.assertEquals(rcs1102.getFindings().iterator().next().getLocation(), "TestNetFramework" + File.separator + "Program.cs" + ",9,11");
        Assert.assertEquals(rcs1163.getFindings().iterator().next().getLocation(), "TestNetFramework" + File.separator + "Program.cs" + ",11,26");
    }

}
