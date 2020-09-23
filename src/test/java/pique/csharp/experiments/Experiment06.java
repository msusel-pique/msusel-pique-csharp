package pique.csharp.experiments;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import pique.csharp.runnable.Assessment;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Category(ExperimentTest.class)
public class Experiment06 {
    @Test
    public void ex06_assess_testnetframework() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 06 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex06_assess_debug/config/ex06_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 06 - assessment finished");
        System.out.println("===================================================================================");
    }
}
