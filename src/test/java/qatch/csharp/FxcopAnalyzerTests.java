package qatch.csharp;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import qatch.model.Measure;
import qatch.model.Property;
import qatch.model.PropertySet;

import java.io.File;
import java.io.IOException;

public class FxcopAnalyzerTests {

    // FxCop analysis needs a compiled CSharp project located at 'src' in order to work
    private final File src = new File("../../MSUSEL/sample-analysis-projects/csharp/SimpleCSharp");
    private final File dest = new File("src/test/output");
    private final File rulesDir = new File(SingleProjectEvaluation.TOOLS_LOCATION + File.separator +
            "FxCop"+ File.separator + "Rules");

    @Test
    public void testAnalyze() throws IOException {
        clean();

        Measure measure01 = new Measure(
                1,
                "metricName01",
                rulesDir.toString() + File.separator + "DesignRules.dll",
                FxcopAnalyzer.TOOL_NAME);
        Measure measure02 = new Measure(
                1,
                "metricName02",
                rulesDir.toString() + File.separator + "DesignRules.dll",
                FxcopAnalyzer.TOOL_NAME);
        Property property01 = new Property("propertyName01", measure01);
        Property property02 = new Property("propertyName02", measure02);

        PropertySet ps = new PropertySet();
        ps.addProperty(property01);
        ps.addProperty(property02);

        FxcopAnalyzer analyzer = new FxcopAnalyzer();
        analyzer.analyze(this.src, this.dest, ps);

        File result01 = new File(dest + File.separator + property01.getName() + ".xml");
        File result02 = new File(dest + File.separator + property02.getName() + ".xml");

        // XML file exists in expected location with correct name
        Assert.assertTrue(result01.exists());
        Assert.assertTrue(result02.exists());
        Assert.assertTrue(result01.isFile());
        Assert.assertTrue(result02.isFile());
        Assert.assertEquals("propertyName01.xml", result01.getName());
        Assert.assertEquals("propertyName02.xml", result02.getName());

        // XML file has expected number of bytes
        Assert.assertEquals(2844, result01.length(), 500);
        Assert.assertEquals(result01.length(), result02.length(), 500);

        clean();
    }

    private void clean() throws IOException {
        File output = this.dest;
        if (output.exists()) {
            FileUtils.cleanDirectory(output);
        }
        else output.mkdirs();
    }

}
