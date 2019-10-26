package qatch.csharp;

import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import qatch.analysis.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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
public class Roslynator extends Tool implements ITool {

    private Path toolsDirectory;
    private Path msBuild;

    /**
     * Constructor.
     * Roslynator analsis needs the MSBuild.exe path
     * (e.g. "C:/Program Files (x86)/Microsoft Visual Studio/2019/Community/MSBuild/Current/Bin")
     *
     * @param toolsDirectory
     *      Qatch-csharp tools directory location
     * @param msBuild
     *      Path to Bin folder containing MSBuild.exe
     */
    public Roslynator(String name, Path toolConfig, Path toolsDirectory, Path msBuild) {
        super(name, toolConfig);
        this.toolsDirectory = toolsDirectory;
        this.msBuild = msBuild;
    }

    /**
     * @param path
     *      The path to a .sln or .csproj file for the desired solution of project to analyze
     * @return
     *      The path to the .xml analysis results file
     */
    @Override
    public Path analyze(Path path) {

        String sep = File.separator;
        ProcessBuilder pb;
        File tempResults = new File(System.getProperty("user.dir") +"/output/roslynator_output.xml");
        tempResults.getParentFile().mkdirs();

        // strings for CLI call
        String roslynator = toolsDirectory.toAbsolutePath().toString() + sep + "Roslynator" + sep + "bin" + sep + "Roslynator.exe";
        String command = "analyze";
        String assemblyDir = "--analyzer-assemblies=" + toolsDirectory.toString() + sep + "Roslynator" + sep + "bin";
        String msBuild = "--msbuild-path=" + this.msBuild.toString();
        String output = "--output=" + tempResults.toString();
        String target = path.toString();

        if(System.getProperty("os.name").contains("Windows")){
            pb = new ProcessBuilder("cmd.exe", "/c", roslynator, command, assemblyDir, msBuild, output, target);
        } else {
            throw new RuntimeException("Roslynator C# analysis not supported on non-Windows machines.");
        }

        pb.redirectErrorStream(true);
        Process p = null;

        // run the tool
        try { p = pb.start(); }
        catch (IOException e) { e.printStackTrace(); }

        try {
            assert p != null;
            p.waitFor();
        }
        catch (InterruptedException e) { e.printStackTrace(); }

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

    // helper methods
    private Diagnostic findMapMemberByDiagnosticId(Map<String, Diagnostic> diagnostics, String id) {
        if (diagnostics.containsKey(id)) { return diagnostics.get(id); }
        else { return new Diagnostic(id); }
    }
}
