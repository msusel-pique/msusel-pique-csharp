package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class LOCMetricsAnalyzerTests {

    // LOCMetrics analysis needs a compiled CSharp project in order to work
    private final File src = new File("src/test/resources/single_project_eval/TestCsharpProject");
    private final File dest = new File("src/test/output");

    /**
     * Ensures the LOCMetrics tool successfully runs on a small C# project
     * and generates an XML results file in the expected directory with the expected analysis
     * results.
     */
    @Test
    public void testAnalyzeSubroutine() throws IOException {

        LOCMetricsAnalyzer analyzer = new LOCMetricsAnalyzer(Paths.get(System.getProperty("user.dir") + "/src/main/resources/tools"));
        analyzer.analyze(src.toPath(), dest.toPath(), null);

        File results = new File(this.dest + File.separator + src.getName() + "_" + LOCMetricsAnalyzer.TOOL_RESULT_FILE_NAME);

        // XML file exists in expected location with correct name
        Assert.assertTrue(results.exists());
        Assert.assertTrue(results.isFile());
        Assert.assertEquals("TestCsharpProject_LocMetricsFolders.csv", results.getName());

        // XML file has approximate expected number of bytes. If LOCMetrics returns all 0's, the byte size is 101.0
        // A better way to test this would be to parse the XML output for expected entries, but
        // that approach adds substantial run time to the unit test.
        Assert.assertTrue("Is there a compiled C# project located at " + src.toString() + "?", results.length() > 102);
    }

}
