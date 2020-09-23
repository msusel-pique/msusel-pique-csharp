package pique.csharp.experiments;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import pique.csharp.runnable.ModelDeriver;

import java.nio.file.Path;
import java.nio.file.Paths;

@Category(ExperimentTest.class)
public class Experiment01 {

    @Test
    public void ex01_derive_default_mechanics() {
        Path configFile = Paths.get("src/test/resources/ex01_derive_default_mechanics/config/ex01_quality_model_deriver.properties");
        ModelDeriver.main(new String[] { configFile.toString() });
        System.out.println("\n================================");
        System.out.println("\tExperiment 01 - derive using default mechanics finished");
        System.out.println("================================\n");
    }

}
