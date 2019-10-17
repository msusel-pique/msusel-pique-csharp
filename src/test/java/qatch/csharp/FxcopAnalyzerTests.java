package qatch.csharp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import qatch.analysis.Measure;
import qatch.model.Property;
import qatch.model.PropertySet;

import java.io.File;
import java.io.IOException;

public class FxcopAnalyzerTests {

    // FxCop analysis needs a compiled CSharp project located at 'src' in order to work
    private final File src = new File("src/test/resources/single_project_eval/TestCsharpProject");
    private final File dest = new File("src/test/output");
    private final File toolsDir = new File("src/main/resources/tools");
    private final File rulesDir = new File(toolsDir, "FxCop/Rules");


    @Test
    public void testAnalyze() throws IOException {

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

        FxcopAnalyzer analyzer = new FxcopAnalyzer(toolsDir.toPath());
        analyzer.analyze(this.src.toPath(), this.dest.toPath(), ps);

        File result01 = new File(dest + File.separator + src.getName() + "_" + property01.getName() + ".xml");
        File result02 = new File(dest + File.separator + src.getName() + "_" + property02.getName() + ".xml");

        // XML file exists in expected location with correct name
        Assert.assertTrue(result01.exists());
        Assert.assertTrue(result02.exists());
        Assert.assertTrue(result01.isFile());
        Assert.assertTrue(result02.isFile());
        Assert.assertEquals("TestCsharpProject_propertyName01.xml", result01.getName());
        Assert.assertEquals("TestCsharpProject_propertyName02.xml", result02.getName());

        // XML file has expected number of bytes
        Assert.assertEquals(2844, result01.length(), 500);
        Assert.assertEquals(result01.length(), result02.length(), 500);
    }

}
