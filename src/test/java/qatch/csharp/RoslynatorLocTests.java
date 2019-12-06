package qatch.csharp;

import org.junit.Assert;
import org.junit.Test;
import qatch.analysis.IToolLOC;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class RoslynatorLocTests {

    private final String TARGET_LOC  = "src/test/resources/projects/TestNetFramework";
    private final Path LOC_ROOT = Paths.get("src/main/resources/Roslynator");

    @Test
    public void testAnalyzeLinesOfCode() throws IOException {
        Properties properties = new Properties();
        properties.load((new FileInputStream("src/test/resources/config/single_project_evaluation.properties")));

        IToolLOC tool = new RoslynatorLoc(LOC_ROOT, Paths.get(properties.getProperty("msbuild.bin")));
        Integer result = tool.analyzeLinesOfCode(Paths.get(TARGET_LOC));

        Assert.assertEquals(39, result, 0);
    }

}
