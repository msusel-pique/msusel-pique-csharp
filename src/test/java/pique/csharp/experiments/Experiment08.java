package pique.csharp.experiments;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import pique.csharp.runnable.Assessment;
import pique.csharp.runnable.ModelDeriver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Category(ExperimentTest.class)
public class Experiment08 {

    @Test
    public void ex08A_assess_control() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08A - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex08_vitro_changes/config/ex08A_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08A - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex08B_assess_flaws() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08B - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex08_vitro_changes/config/ex08B_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08B - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex08C_assess_fixes() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08C - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex08_vitro_changes/config/ex08C_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08C - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex08D_comp_matrix_derive_control() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08D - beginning derive");
        System.out.println("===================================================================================");

        Path deriveConfigFile = Paths.get("src/test/resources/ex08_vitro_changes/config/ex08D_derive_control.properties");
        ModelDeriver.main(new String[] { deriveConfigFile.toString() });

        System.out.println("===================================================================================");
        System.out.println("\tExperiment 08D - derive finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex08D_comp_matrix_assess_control() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08D - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex08_vitro_changes/config/ex08D_assessment_control.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08D - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex08D_comp_matrix_derive_assess_exp() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08D - beginning derive");
        System.out.println("===================================================================================");

        Path deriveConfigFile = Paths.get("src/test/resources/ex08_vitro_changes/config/ex08D_derive_experiment.properties");
        ModelDeriver.main(new String[]{deriveConfigFile.toString()});

        System.out.println("===================================================================================");
        System.out.println("\tExperiment 08D - derive finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex08D_comp_matrix_assess_experiment() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08D - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex08_vitro_changes/config/ex08D_assessment_experiment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 08D - assessment finished");
        System.out.println("===================================================================================");
    }
}
