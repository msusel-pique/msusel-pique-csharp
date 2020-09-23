package pique.csharp.experiments;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import pique.csharp.runnable.ModelDeriver;
import pique.csharp.runnable.Assessment;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Category(ExperimentTest.class)
public class Experiment02 {

    @Test
    public void ex02A_derive_then_assess_default_mechanics() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02A - beginning derive with control group");
        System.out.println("===================================================================================");

        Path configFile = Paths.get("src/test/resources/ex02_derive_modified_mechanics/config/ex02A_quality_model_deriver.properties");
        ModelDeriver.main(new String[] { configFile.toString() });

        System.out.println("------------------------");
        System.out.println("\tExperiment 02A - derive using control group finished");
        System.out.println("\tExperiment 02A - beginning assessment");
        System.out.println("------------------------");

        Path assessConfigFile = Paths.get("src/test/resources/ex02_derive_modified_mechanics/config/ex02A_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02A - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex02A_just_assess_default_mechanics() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02A - beginning assessment with control group");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex02_derive_modified_mechanics/config/ex02A_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02A - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex02B_derive_modify_normalization() {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02B - beginning derive");
        System.out.println("===================================================================================");

        Path configFile = Paths.get("src/test/resources/ex02_derive_modified_mechanics/config/ex02B_quality_model_deriver.properties");
        ModelDeriver.main(new String[] { configFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02B - derive finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex02C_derive_modify_benchmarker_assess() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02C - beginning derive");
        System.out.println("===================================================================================");

        Path deriveConfigFile = Paths.get("src/test/resources/ex02_derive_modified_mechanics/config/ex02C_quality_model_deriver.properties");
        ModelDeriver.main(new String[] { deriveConfigFile.toString() });

        System.out.println("------------------------");
        System.out.println("\tExperiment 02C - derive finished");
        System.out.println("\tExperiment 02C - beginning assessment...");
        System.out.println("------------------------");

        Path assessConfigFile = Paths.get("src/test/resources/ex02_derive_modified_mechanics/config/ex02C_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02C - assessment finished");
        System.out.println("===================================================================================");
    }

    @Test
    public void ex02D_derive_modify_weights()  {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02D - beginning derive");
        System.out.println("===================================================================================");

        Path deriveConfigFile = Paths.get("src/test/resources/ex02_derive_modified_mechanics/config/ex02D_quality_model_deriver.properties");
        ModelDeriver.main(new String[] { deriveConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02D - derive finished");
        System.out.println("=================================================================================");
    }

    @Test
    public void ex02E_assess_security_evaluation() throws IOException {
        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02E - beginning assessment");
        System.out.println("===================================================================================");

        Path assessConfigFile = Paths.get("src/test/resources/ex02_derive_modified_mechanics/config/ex02E_assessment.properties");
        Assessment.main(new String[] { assessConfigFile.toString() });

        System.out.println("\n=================================================================================");
        System.out.println("\tExperiment 02E - assessment finished");
        System.out.println("===================================================================================");

    }

}
