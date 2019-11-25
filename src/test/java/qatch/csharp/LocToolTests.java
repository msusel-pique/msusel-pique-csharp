package qatch.csharp;

import org.junit.Assert;
import org.junit.Test;
import qatch.analysis.IToolLOC;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class LocToolTests {

    private final String TOOL_NAME = "LoC",
                         TOOLS_LOC   = "src/main/resources/tools",
                         TARGET_LOC  = "src/test/resources/projects/TestNetFramework";

    @Test
    public void testAnalyze() throws IOException {
        Properties properties = new Properties();
        properties.load((new FileInputStream("src/test/resources/config/config.properties")));

        IToolLOC tool = new LocTool(TOOL_NAME, Paths.get(TOOLS_LOC), Paths.get(properties.getProperty("MSBUILD_BIN")));
        Integer result = tool.analyze(Paths.get(TARGET_LOC));

        Assert.assertEquals(39, result, 0);
    }

}
