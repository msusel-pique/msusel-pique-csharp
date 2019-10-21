package qatch.csharp;

import qatch.analysis.ITool;
import qatch.analysis.Measure;
import qatch.analysis.Tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
    public List<Measure> parse(Path path) {
        return null;
    }
}
