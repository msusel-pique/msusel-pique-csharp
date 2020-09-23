package pique.csharp;

import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pique.analysis.ITool;
import pique.evaluation.DefaultDiagnosticEvaluator;
import pique.model.Diagnostic;
import pique.model.Finding;
import pique.utility.FileUtility;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ITool implementation static analyasis tool class.
 *
 * Roslynator download: https://marketplace.visualstudio.com/items?itemName=josefpihrt.Roslynator2019
 * Roslynator repo: https://github.com/JosefPihrt/Roslynator
 * Roslynator is a collection of C# static analysis tools that can be run on .NET core, .NET framework,
 * and .NET standard projects so long as a solution (.sln) file exists.
 * Roslynator is able to generate both static analysis findings and loc/lloc metrics.
 *
 * The .exe should be kept in resources/tools.
 */
public class RoslynatorAnalyzer extends RoslynatorTool implements ITool {

    // Fields
    private Path msBuild;


    /**
     * Constructor.
     * Roslynator analsis needs the MSBuild.exe path
     * (e.g. "C:/Program Files (x86)/Microsoft Visual Studio/2019/Community/MSBuild/Current/Bin")
     *
     * @param toolRoot
     *      Qatch-csharp tools directory location
     * @param msBuild
     *      Path to Bin folder containing MSBuild.exe
     */
    public RoslynatorAnalyzer(Path toolRoot, Path msBuild) {
        super("Roslynator", toolRoot);
        this.msBuild = msBuild;
    }


    // Methods
    /**
     * @param path
     *      The path to a .sln or .csproj file for the desired solution of project to analyze
     * @return
     *      The path to the .xml analysis results file
     */
    @Override
    public Path analyze(Path path) {

        path = path.toAbsolutePath();
        String sep = File.separator;
        File tempResults = new File(System.getProperty("user.dir") +"/out/roslynator_output.xml");
        tempResults.getParentFile().mkdirs();

        // Append .sln or .csproj file to path
        // TODO: refactor to method and find better way that doesn't use stacked if statements.
        Set<String> targetFiles = FileUtility.findFileNamesFromExtension(path, ".sln", 1);
        if (targetFiles.size() == 1) {
            path = Paths.get(path.toString(), targetFiles.iterator().next() + ".sln");
        }
        else if (targetFiles.size() > 1) {
            throw new RuntimeException("More than one .sln file exists in the give path root directory. " +
                    "Ensure the directory has only one .sln file to target.");
        }
        else {
            targetFiles = FileUtility.findFileNamesFromExtension(path, ".csproj", 1);
            if (targetFiles.size() == 1) {
                path = Paths.get(path.toString(), targetFiles.iterator().next() + ".csproj");
            }
            else if (targetFiles.size() > 1) {
                throw new RuntimeException("A .sln file not found and more than one .csproj file exists in the give path root directory. " +
                        "Ensure the directory has only one .csproj file to target.");
            }
        }

        // Strings for CLI call
        String roslynator = getExecutable().toAbsolutePath().toString();
        String command = "analyze";
        String assemblyDir = "--analyzer-assemblies=" + getToolRoot().toAbsolutePath().toString()  + sep + "bin";
        String msBuild = "--msbuild-path=" + this.msBuild.toString();
        String output = "--output=" + tempResults.toString();
        String target = path.toString();

        if(System.getProperty("os.name").contains("Windows")){
        } else {
            throw new RuntimeException("Roslynator C# analysis not supported on non-Windows machines.");
        }

        // Run the tool
        System.out.println("roslynator: beginning static analysis.\n\tTarget: " + path.toString());

        Process p = null;
        try {
            p = new ProcessBuilder(roslynator, command, assemblyDir, msBuild, output, target).start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = stdInput.readLine()) != null) {
                System.out.println("roslynator: " + line);
            }
            p.waitFor();
        }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }

        // Assert result file was created
        if (!tempResults.isFile()) {
            throw new RuntimeException("Roslynator.analyze() did not generate a results file in the expected location");
        }

        return tempResults.toPath();
    }


    @Override
    public Map<String, Diagnostic> parseAnalysis(Path path) {

        Map<String, Diagnostic> diagnostics = new HashMap<>();

        // XML parsing (shamelessly borrowed from https://stackoverflow.com/questions/11720999/simplest-way-to-parse-this-xml-in-java)
        try {
            // load XML
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(path.toFile());

            // load XPath
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("//Diagnostics/Diagnostic");

            // collect set of diagnostics
            NodeList diagnosticNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < diagnosticNodes.getLength(); i++) {  // create diagnostic objects
                Node diagnosticElement = diagnosticNodes.item(i);
                String diagnosticId = ((DeferredElementImpl) diagnosticElement).getAttributeNode("Id").getValue();
                Diagnostic diagnostic = findMapMemberByDiagnosticId(diagnostics, diagnosticId);
                NodeList diagnosticChildren = diagnosticElement.getChildNodes();

                // attach findings
                Finding finding = new Finding();
                for (int j = 0; j < diagnosticChildren.getLength(); j++) {
                    Node diagnosticChild = diagnosticChildren.item(j);
                    switch (diagnosticChild.getNodeName()) {
                        case "FilePath":
                            finding.setFilePath(diagnosticChild.getTextContent());
                            break;
                        case "Location":
                            finding.setLineNumber(Integer.parseInt(((DeferredElementImpl) diagnosticChild).getAttribute("Line")));
                            finding.setCharacterNumber(Integer.parseInt(((DeferredElementImpl) diagnosticChild).getAttribute("Character")));
                            break;
                        case "Severity":
                            switch (diagnosticChild.getTextContent()) {
                                case "Info":
                                    finding.setSeverity(2);
                                    break;
                                case "Warning":
                                    finding.setSeverity(3);
                                    break;
                                case "Error":
                                    finding.setSeverity(4);
                                    break;
                                default:
                                    finding.setSeverity(1);
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                }
                diagnostic.setFinding(finding);

                // add parsed diagnostic with attached finding objects to collection
                diagnostics.put(diagnosticId, diagnostic);
            }

        } catch (ParserConfigurationException e) {
            System.out.println("Bad parser configuration");
            e.printStackTrace();
        } catch (SAXException e) {
            System.out.println("SAX error loading the file.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Error reading the file.");
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            System.out.println("Bad XPath Expression");
            e.printStackTrace();
        }

        return diagnostics;
    }


    // Helper methods

    private Diagnostic findMapMemberByDiagnosticId(Map<String, Diagnostic> diagnostics, String id) {
        if (diagnostics.containsKey(id)) { return diagnostics.get(id); }
        else { return new Diagnostic(id, "", "Roslynator", new DefaultDiagnosticEvaluator()); }
    }


    @Override
    public Path initialize(Path toolRoot) {
        return roslynatorInitializeToTempFolder();
    }

    @Override
    public String getName() {
        return "Roslynator";
    }
}
