package pique.csharp.experiments;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import pique.csharp.runnable.Assessment;
import pique.csharp.runnable.ModelDeriver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Category(ExperimentTest.class)
public class Experiment09 {

    @Test
    public void ex09_oct22() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Oct 22 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex09_output_trustability/config/ex09_assessment_oct22.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Oct 22 - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex09_dec04() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Dec 04 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex09_output_trustability/config/ex09_assessment_dec04.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Dec 04 - assessment finished");
        System.out.println("===================================================================================");
    }


    @Test
    public void ex09_jan21() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Jan 21 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex09_output_trustability/config/ex09_assessment_jan21.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Jan 21 - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex09_jan30() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Jan 30 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex09_output_trustability/config/ex09_assessment_jan30.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Jan 30 - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex09_feb10() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Feb 10 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex09_output_trustability/config/ex09_assessment_feb10.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Feb 20 - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex09_apr15() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Apr 15 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex09_output_trustability/config/ex09_assessment_apr15.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Apr 15 - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex09_mar25() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Mar 25 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex09_output_trustability/config/ex09_assessment_mar25.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Mar 25 - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex09_mar27() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Mar 27 - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex09_output_trustability/config/ex09_assessment_mar27.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 09 Mar 27 - assessment finished");
        System.out.println("===================================================================================");
    }

}
