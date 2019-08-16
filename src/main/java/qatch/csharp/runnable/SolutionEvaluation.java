package qatch.csharp.runnable;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import qatch.analysis.*;
import qatch.csharp.*;
import qatch.evaluation.EvaluationResultsExporter;
import qatch.evaluation.Project;
import qatch.evaluation.ProjectCharacteristicsEvaluator;
import qatch.evaluation.ProjectEvaluator;
import qatch.model.*;
import qatch.utility.FileUtility;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This executable class is responsible for producing quality analysis reports on all modules contained within
 * a C# solution.  The driver supports deployed JAR functionality when packaged with dependencies.
 */
public class SolutionEvaluation {

    final static Path ROOT = Paths.get(System.getProperty("user.dir"));
    /**
     * Run single project evaluations on a .NET Framework solution in batch mode to produce analysis results
     * for every .csproj project.  Assumes a derived c# quality model already exists.
     *
     * @param args configuration array in following order:
     *             0: path to the solution to be evaluated root folder
     *             1: path to folder to place analysis results
     *    These arg paths can be relative or full path
     */
    public static void main(String[] args) {

        // useful constants
        final boolean RERUN_TOOLS = false;  // TODO: remove this once no longer testing

        final Path SOLUTION;
        final Path OUTPUT;
        final Path RESOURCES;
        final Path ANALYSIS;

        final String QM_NAME = "qualityModel_iso25k_csharp.xml";
        final String projectRootFlag = ".csproj";   // how to know you are at a project root when recursing through files

        // initialize
        if (args == null || args.length != 2) {
            throw new RuntimeException("Incorrect input parameters given. Be sure to include " +
                    "\n\t(0) path to the solution to be evaluated root folder," +
                    "\n\t(1) path to folder to place analysis results.");
        }
        SOLUTION = Paths.get(args[0]);
        OUTPUT = Paths.get(args[1]);
        ANALYSIS = new File(OUTPUT.toFile(), "analysis_results").toPath();

        OUTPUT.toFile().mkdirs();
        ANALYSIS.toFile().mkdirs();

        // extract resources
        RESOURCES = extractResources(OUTPUT);

        // run single project evaluation on each project found in the target solution folder
        Set<Path> projectRoots = FileUtility.multiProjectCollector(SOLUTION, projectRootFlag);
        System.out.println(projectRoots.size() + " projects found for analysis.");

        projectRoots.forEach(p -> {
            System.out.println("Beginning analysis on " + p.getFileName());

            // TODO: eventually all these calls will likely be moved to Qatch framework
            QualityModel qualityModel = makeNewQM(Paths.get(RESOURCES.toString() + "/models/" + QM_NAME));
            Project project = makeProject(p);
            if (RERUN_TOOLS) { runTools(Paths.get(project.getPath()), ANALYSIS, qualityModel); }
            project.setMetrics(getMetricsFromImporter(
                    Paths.get(ANALYSIS.toString() + "/" + project.getName() + "/metrics")));
            project.setIssues(getIssuesFromImporter(
                    Paths.get(ANALYSIS.toString() + "/" + project.getName() + "/findings")));
            project.cloneProperties(qualityModel);
            aggregateAndNormalize(project);
            evaluate(project, qualityModel);
            export(project, ANALYSIS);
        });
    }


    private static void aggregateAndNormalize(Project project) {
        IMetricsAggregator metricsAggregator = new LOCMetricsAggregator();
        IFindingsAggregator findingsAggregator = new FxcopAggregator();

        metricsAggregator.aggregate(project);
        findingsAggregator.aggregate(project);

        for(int i = 0; i < project.getProperties().size(); i++){
            Property property =  project.getProperties().get(i);
            property.getMeasure().calculateNormValue();
        }
    }


    /**
     * Evaluates properties first, then characteristics, then the TQI
     */
    private static void evaluate(Project project, QualityModel qualityModel) {
        ProjectEvaluator evaluator = new ProjectEvaluator();
        ProjectCharacteristicsEvaluator charEvaluator = new ProjectCharacteristicsEvaluator();

        // evaluate properties
        evaluator.evaluateProjectProperties(project);

        try {
            // evaluate characteristics
            for (int i = 0; i < qualityModel.getCharacteristics().size(); i++) {
                //Clone the characteristic and add it to the CharacteristicSet of the current project
                Characteristic c = (Characteristic) qualityModel.getCharacteristics().get(i).clone();
                project.getCharacteristics().addCharacteristic(c);
            }
            charEvaluator.evaluateProjectCharacteristics(project);

            // evaluate TQI
            project.setTqi((Tqi) qualityModel.getTqi().clone());
            project.calculateTQI();
        }
        catch (CloneNotSupportedException e) { e.printStackTrace(); }
    }


    private static void export(Project project, Path parentDir) {
        String name = project.getName();
        File evalResults = new File(parentDir.toFile(), name + File.separator + name + "_evalResults.json");
        EvaluationResultsExporter.exportProjectToJson(project, evalResults.getAbsolutePath());
    }


    private static Vector<IssueSet> getIssuesFromImporter(Path path) {
        IFindingsResultsImporter findingsImporter = new FxcopResultsImporter();
        File[] results = path.toFile().listFiles();
        Vector<IssueSet> issues = new Vector<>();

        for (File resultFile : results) {
            try { issues.add(findingsImporter.parse(resultFile.toPath())); }
            catch (IOException | ParserConfigurationException | SAXException e) { e.printStackTrace(); }
        }

        return issues;
    }


    private static MetricSet getMetricsFromImporter(Path path) {
        // TODO: this functionality will eventually be moved to a qatch-min generic ResultsImporter class
        IMetricsResultsImporter metricsImporter = new LOCMetricsResultsImporter();
        File[] results = path.toFile().listFiles();

        if (results == null) throw new RuntimeException("Scanner results directory [" + path.toString() + "] has no files from static analysis.");

        for (File resultFile : results) {
            if (resultFile.getName().toLowerCase().contains("locmetrics")) {
                try { return metricsImporter.parse(resultFile.toPath()); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }

        throw new RuntimeException("Unable to find a LocMetrics file in directory " + path.toFile());
    }

    private static QualityModel makeNewQM(Path qmLocation) {
        QualityModelLoader qmImporter = new QualityModelLoader(qmLocation.toString());
        return qmImporter.importQualityModel();
    }


    private static Project makeProject(Path p) {
        Project project = new Project();
        project.setPath(p.toAbsolutePath().toString());
        project.setName(p.getFileName().toString());
        return project;
    }


    private static void runTools(Path projectDir, Path resultsDir, QualityModel qualityModel) {
        IAnalyzer metricsAnalyzer = new LOCMetricsAnalyzer();
        IAnalyzer findingsAnalyzer = new FxcopAnalyzer();

        File projFolder = new File(resultsDir.toFile(), projectDir.getFileName().toString());
        File findings = new File(projFolder, "findings");
        File metrics = new File(projFolder, "metrics");

        findings.mkdirs();
        metrics.mkdirs();

        metricsAnalyzer.analyze(projectDir, metrics.toPath(), qualityModel.getProperties());
        findingsAnalyzer.analyze(projectDir, findings.toPath(), qualityModel.getProperties());
    }


    private static Path extractResources(Path destination)  {

        String protocol = SolutionEvaluation.class.getResource("").getProtocol();
        File resourcesDirectory = new File(destination.toFile(), "resources");
        resourcesDirectory.mkdirs();

        if (Objects.equals(protocol, "jar")) {
            try { extractResourcesToTempFolder(resourcesDirectory.toPath()); }
            catch (IOException | URISyntaxException e) { e.printStackTrace(); }
        }

        else if (Objects.equals(protocol, "file")) {
            File models = new File(ROOT + "/src/main/resources/tools");
            File tools = new File(ROOT + "/src/main/resources/models");
            try {
                FileUtils.copyDirectoryToDirectory(models , resourcesDirectory);
                FileUtils.copyDirectoryToDirectory(tools , resourcesDirectory);
            }
            catch (IOException e) {  e.printStackTrace(); }
        }

        else { throw new RuntimeException("Unable to determine if project is running from IDE or JAR"); }

        return resourcesDirectory.toPath();
    }


    /**
     * Code from https://stackoverflow.com/questions/1529611/how-to-write-a-java-program-which-can-extract-a-jar-file-and-store-its-data-in-s/1529707#1529707
     * by user Jamesst20
     *
     * Used when running program as a JAR.
     *
     * Takes resources in the resources folder within the JAR and copies them to a
     * resources folder in the same directory as the JAR. Also moves the ant build.xml
     * file to root directory.
     */
    private static void extractResourcesToTempFolder(Path destination) throws IOException, URISyntaxException {
        File jarFile = new File(SolutionEvaluation
                .class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());

        //Recursively build resources folder from JAR sibling to JAR file
        JarFile jar = new JarFile(jarFile.getPath());
        Enumeration<JarEntry> enums = jar.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().startsWith("models") || entry.getName().startsWith("tools")) {
                File toWrite = new File(destination.toFile(), entry.getName());
                if (entry.isDirectory()) {
                    toWrite.mkdirs();
                    continue;
                }
                InputStream in = new BufferedInputStream(jar.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWrite));
                byte[] buffer = new byte[2048];
                for (;;) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nBytes);
                }
                out.flush();
                out.close();
                in.close();
            }
        }
    }

}
