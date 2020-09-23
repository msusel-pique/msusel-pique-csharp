package pique.csharp.experiments;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import pique.csharp.runnable.Assessment;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Category(ExperimentTest.class)
public class Experiment03 {

    @Test
    public void ex03_assess_testnetframework() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 03 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex03_operationalize_assessment/config/ex03_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 03 - assessment finished");
        System.out.println("===================================================================================");
    }

}
