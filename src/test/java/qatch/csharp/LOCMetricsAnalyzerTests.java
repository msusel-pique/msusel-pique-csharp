package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LOCMetricsAnalyzerTests {

    // LOCMetrics analysis needs a compiled CSharp project located at 'src' in order to work
    private final File src = new File("src/test/resources/compiled_projects/SimpleCSharp");
    private final File dest = new File("src/test/output");

    /**
     * Ensures the LOCMetrics tool successfully runs on a small C# project
     * and generates an XML results file in the expected directory with the expected analysis
     * results.
     */
    @Test
    public void testAnalyzeSubroutine() throws IOException {
        clean();

        LOCMetricsAnalyzer analyzer = new LOCMetricsAnalyzer();
        analyzer.analyze(src.toPath(), dest.toPath(), null);

        File results = new File(this.dest + File.separator + src.getName() + "_" + LOCMetricsAnalyzer.TOOL_RESULT_FILE_NAME);

        // XML file exists in expected location with correct name
        Assert.assertTrue(results.exists());
        Assert.assertTrue(results.isFile());
        Assert.assertEquals("SimpleCSharp_LocMetricsFolders.csv", results.getName());

        // XML file has approximate expected number of bytes. If LOCMetrics returns all 0's, the byte size is 101.0
        // A better way to test this would be to parse the XML output for expected entries, but
        // that approach adds substantial run time to the unit test.
        Assert.assertTrue("Is there a compiled C# project located at " + src.toString() + "?", results.length() > 102);
    }

    private void clean() throws IOException {
        File output =  this.dest;
        if (output.exists()) {
            FileUtils.cleanDirectory(output);
        }
        else output.mkdirs();
    }

}
