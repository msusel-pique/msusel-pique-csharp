package qatch.csharp;

import qatch.analysis.Tool;
import qatch.utility.FileUtility;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Abstract class for common functions of Roslynator tool needs
 */
public abstract class RoslynatorTool extends Tool {

    public RoslynatorTool(String name, Path toolRoot) {
        super(name, toolRoot);
    }


    protected Path roslynatorInitializeToTempFolder() {
        String protocol = RoslynatorTool.class.getResource("").getProtocol();
        Path tempResourceDirectory = Paths.get(System.getProperty("user.dir"), "resources");

        switch (protocol) {
            case "file":
                Path roslynatorResource = getToolRoot();
                tempResourceDirectory = FileUtility.extractResourcesAsIde(tempResourceDirectory, roslynatorResource);
                break;
            case "jar":
                try {
                    File jarFile = new File(RoslynatorAnalyzer
                            .class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());
                    String resourceName = "Roslynator";
                    tempResourceDirectory = FileUtility.extractResourcesAsJar(jarFile, tempResourceDirectory, resourceName);
                }
                catch (URISyntaxException e) { e.printStackTrace(); }
                break;
            default:
                throw new RuntimeException("Protocol did not match with 'file' or 'jar'");
        }

        setToolRoot(Paths.get(tempResourceDirectory.toString(), getName()));
        return Paths.get(getToolRoot().toString(), "bin", "Roslynator.exe");
    }
}
