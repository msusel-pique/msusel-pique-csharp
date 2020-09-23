package pique.csharp.calibration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pique.analysis.ITool;
import pique.calibration.DefaultBenchmarker;
import pique.calibration.IBenchmarker;
import pique.calibration.RInvoker;
import pique.evaluation.Project;
import pique.model.Diagnostic;
import pique.model.QualityModel;
import pique.utility.FileUtility;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LinearBenchmarker implements IBenchmarker {

    @Override
    public Map<String, Double[]> deriveThresholds(Path benchmarkRepository, QualityModel qmDescription, Set<ITool> tools, String projectRootFlag, Path analysisResults, Path rThresholdsOutput) {

        // Collect root paths of each benchmark project
        Set<Path> projectRoots = FileUtility.multiProjectCollector(benchmarkRepository, projectRootFlag);
        ArrayList<Project> projects = new ArrayList<>();

        System.out.println("* Beginning repository benchmark analysis");
        System.out.println(projectRoots.size() + " projects to analyze.\n");

        int totalProjects = projectRoots.size();
        int counter = 0;

        for (Path projectPath : projectRoots) {

            counter++;

            // Instantiate new project object
            Project project = new Project(projectPath.getFileName().toString(), projectPath, qmDescription);

            // Run the static analysis tools process
            Map<String, Diagnostic> allDiagnostics = new HashMap<>();
            tools.forEach(tool -> {
                Path analysisOutput = tool.analyze(projectPath);
                allDiagnostics.putAll(tool.parseAnalysis(analysisOutput));
            });

            // Run LOC tool to set lines of code
            project.setLinesOfCode((int) allDiagnostics.get("loc").getValue());

            // Apply collected diagnostics (containing findings) to the project
            allDiagnostics.forEach((diagnosticName, diagnostic) -> {
                project.addFindings(diagnostic);
            });

            // Evaluate project up to Measure level (normalize does happen first)
            project.evaluateMeasures();

            // Add new project (with tool findings information included) to the list
            projects.add(project);

            // Print information
            System.out.println("\n\tFinished analyzing project " + project.getName());
            System.out.println("\t" + counter + " of " + totalProjects + " analyzed.\n");
        }

        // Create [Project_Name:Measure_Values] matrix file
        DefaultBenchmarker.createProjectMeasureMatrix(projects, analysisResults);

        // Generate thresholds
        return rMeasureMedians(rThresholdsOutput, analysisResults);

    }

    @Override
    public double utilityFunction(double input, Double[] thresholds, boolean positive) {
        if (thresholds.length != 1) throw new RuntimeException("LinearBenchmarker.utilityFunction expectes the measure's thresholds[].size == 1");

        Double middleBound = thresholds[0];

        if (positive) {
            if (input >= middleBound) return 1.0;
            else return 0.0;
        }

        else {
            if (input <= middleBound) return 1.0;
            else return 0.0;
        }
    }

    @Override
    public String getName() {
        return "pique.csharp.calibration.LinearBenchmarker";
    }


    Map<String, Double[]> rMeasureMedians(Path output, Path analysisResults) {

        // Precondition check
        if (!analysisResults.toFile().isFile()) {
            throw new RuntimeException("Benchmark analysisResults field must point to an existing file");
        }

        // Prepare temp file for R Script results
        output.toFile().mkdirs();
        File thresholdsFile = new File(output.toFile(), "threshold.json");

        // R script expects the directory containining the analysis results as a parameter
        Path analysisDirectory = analysisResults.getParent();

        // Run R Script
        RInvoker.executeRScript(RInvoker.Script.THRESHOLD, analysisDirectory, output, FileUtility.getRoot());

        if (!thresholdsFile.isFile()) {
            throw new RuntimeException("Execution of R script did not result in an existing file at " + thresholdsFile.toString());
        }

        // Remove min and max
        String jsonString = "";
        try {
            FileReader fr = new FileReader(thresholdsFile.toString());
            JsonArray jsonThreshArray = new JsonParser().parse(fr).getAsJsonArray();
            fr.close();

            jsonThreshArray.forEach(measure -> {
                measure.getAsJsonObject().remove("t1");
                measure.getAsJsonObject().remove("t3");
            });

            jsonString = jsonThreshArray.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save the results
        try {
            FileWriter writer = new FileWriter(thresholdsFile.toString());
            writer.write(jsonString);
            writer.close();
        } catch(IOException e){ System.out.println(e.getMessage());  }


        // Build object representation of data from R script
        Map<String, Double[]> thresholds = new HashMap<>();
        try {
            FileReader fr = new FileReader(thresholdsFile.toString());
            JsonArray jsonEntries = new JsonParser().parse(fr).getAsJsonArray();

            for (JsonElement entry : jsonEntries) {
                JsonObject jsonProperty = entry.getAsJsonObject();
                String name = jsonProperty.getAsJsonPrimitive("_row").getAsString().replaceAll("\\.", " ");
                Double[] threshold = new Double[] {
                        jsonProperty.getAsJsonPrimitive("t2").getAsDouble()
                };
                thresholds.put(name, threshold);
            }

            fr.close();
        }
        catch (IOException e) { e.printStackTrace(); }

        return thresholds;
    }
}
