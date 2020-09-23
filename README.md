# msusel-pique-csharp
This repository represents a C# actualization of the [PIQUE](https://github.com/msusel-pique/msusel-pique) quality analysis platform.
This project integrates the C# static analysis framework tool, *Roslynator*, and provides example extensions of the default weighting, benchmarking, normalizing, and evaluation strategies provided by PIQUE.

Additionally, this project provides the test cases and exercises used to verify components of the PIQUE system from the thesis backing this project.  


## Build Environment
- Java 8+
- Maven
- MSBuild
    - Can be obtained from a Visual Studio 2017 install; found in `C:/Program Files (x86)/Microsoft Visual Studio/2017/Community/MSBuild/Current/Bin`.  This is the easiest way to get an MSBuild version that cooperates with the .NET Framework 4.x projects in the C# benchmark repository.
- R 3.6.1 (only needed for model derivation)
  - with library 'jsonlite'
  - The version is necessary to work with jsonlite.  This R dependency current exists for legacy sake, but should be depreciated as soon as possible.

## Building
1. Ensure the [Build Environment](#build-environment) requirements are met.
1. Ensure *msusel-pique* is installed as a resource as described in the [msusel-pique README](https://github.com/msusel-pique/msusel-pique/blob/master/README.md) (using `mvn install` in the *msusel-pique* cloned root directory).
1. From the root directory of this project...
    - Restore packages with `mvn dependency:resolve`
        - Sometimes some extra tinkering is needed to make sure all dependencies are managed. Make sure you know your IDE or Java CLI and tools.
    - Clean and build with `mvn clean compile`
    - Verify main execution processes work (model derivation and product assessment using integrated test resources) using the integration test profile `mvn clean test -Pit`
        - Runs an integration test verifying a connects C# static analysis tool, Roslynator, runs correctly on a real C# project.
        - Runs an integration test verifying a C# model can be derived using Roslynator against a small benchmark repository.
        - Runs an integration test verifying quality assessment on a real, C# project. 

___

## Running - Run via command line or IDE from compiled source
### Derive a quality model using a benchmark repository
1. Prepare a derivation configuration file
    - Create a `deriver.properties` configuration file that informs the runtime where to look for the quality model description file, benchmark repository, comparison matrices, and the path to place analysis output artifacts.  
        - Refer to `<repository root>/src/test/resources/config/quality_model_deriver.properties` for an example properties file.
        - Refer to `<repository root>/src/test/resources/projects/test_benchmark_repository` for an example benchmark repository.
        - Refer to `<repository root>/src/test/resources/comparison_matrices/test_qm_generator` for example comparison matrices.
1. Ensure the environment described by the configuration file is in place (benchmark repository projects, quality model description, etc.)
1. Run the main method `pique.csharp.runnable.ModelDeriver.main()` passing in a single argument: the path to the `deriver.properties` file.
   - This can be done via the command line, as a test method, or using your IDE's run configuration.
1. After running `ModelDeriver.main()`, a derived quality model (now with edge weights as derived from the comparison matrices and threshold values derived from the benchmark repository) is output to the location `results.directory` defined in the `deriver.properties`.  Intermediary analysis output artifacts are also output to the `benchmarkscan.output`, `rthreshold.output`, and `rweights.output` locations defined in `deriver.properties`.

### Run quality assessment
Now that a derived quality model exists with edge weights and thresholds from [derive a quality model](#derive-a-quality-model-using-a-benchmark-repository), that same model can be used to run real quality assessment on a C# project or solution. 
1. Prepare an evaluation configuration file
    - Create a `evaluate.properties` file that informs the runtime the location of the product under assessment, which quality model file to use, and where to place assessment output.
        - Refer to `<repository root>/src/test/resources/config/single_project_evaluation.properties` for an example properties file.
        - Refer to `<repository root>/src/test/resources/quality_model/test_single_project_eval_qm.json` for an example quality model file.
1. Ensure the environment described by the configuration file is in place (product under assessment and quality model file exists in the correct location).
1. Run the main method `pique.csharp.runnable.Assessment.main()` passing in a single argument: the path to the `evaluate.properties` file.
    - This can be done via the command line, as a test method, or using your IDE's run configuration.
    - This can also be done via a deployed JAR file as described in the following section.
1. After running `Assessment.main()`, a quality model with evaluated node values representing the quality of the product under assessment is output to the location `results.directory` defined in the `evaluate.properties` file.

___

## Deployment - Run quality assessment via an OS-independent JAR 
(todo)
- Create a runnable JAR with `mvn package` from root directory
    - Use `mvn package -Dmaven.test.skip=true` to skip tests if desired
- This produces a .jar file at `<repository root>/target/msusel.pique.csharp-x.x.x-jar-with-dependencies.jar`.
- Make config.properties and point to product under assessment and QM file
- Run quality assessment via command line with `java -jar .\msusel-pique-csharp-0.3.0-jar-with-dependencies.jar .\single_project_evaluation.properties`

___

## Additional Examples
*Note: some of these examples assume an external benchmark repository exists. Reference the paths in the .properties file and the [msusel benchmark repository database](https://github.com/msusel-pique/benchmark-repository-csharp) to get started on such a configuration.*

### Modify the Default Mechanisms Provided by PIQUE
The tests in `src/test/java/pique/csharp/experiments/Experiment02.java` provide examples of overriding PIQUE's default weighter, benchmarker, normalizer, and evaluator with custom strategies.
Note how the classes defined in this project, for example `pique.csharp.calibration.EqualWeighter` and `pique.csharp.evaluation/SecurityEvaluator` are passed in as configuration in the quality model files found in `src/test/resources/ex02_derive_modified_mechanics/config`. 