package qatch.csharp;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RoslynatorTests {

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

        System.out.println("...");
    }

}
