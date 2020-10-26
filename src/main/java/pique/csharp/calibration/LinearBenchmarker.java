package pique.csharp.calibration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pique.analysis.ITool;
import pique.calibration.NaiveBenchmarker;
import pique.calibration.IBenchmarker;
import pique.evaluation.Project;
import pique.model.Diagnostic;
import pique.model.QualityModel;
import pique.utility.FileUtility;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

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

//        Map<String, Measure> measures = qmDescription.getMeasures();
        Map<String, ArrayList<Double>> measureBenchmarkData = new HashMap<>();
        projects.forEach(p -> {
            p.getQualityModel().getMeasures().values().forEach(m -> {
                        if (!measureBenchmarkData.containsKey(m.getName())) {
                            measureBenchmarkData.put(m.getName(), new ArrayList<Double>() {{
                                add(m.getValue());
                            }});
                        }
                        else {
                            measureBenchmarkData.get(m.getName()).add(m.getValue());
                        }
                    }
            );
        });

        Map<String, Double[]> measureThresholds = new HashMap<>();
        measureBenchmarkData.forEach((measureName, measureValues) -> {
            measureThresholds.putIfAbsent(measureName, new Double[2]);
            measureThresholds.get(measureName)[0] = Collections.min(measureValues);
            measureThresholds.get(measureName)[1] = Collections.max(measureValues);

        });

        return measureThresholds;

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

}
