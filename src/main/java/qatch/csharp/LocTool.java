package qatch.csharp;

import qatch.analysis.IToolLOC;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * IToolLOC implementation using Roslynator CLI
 *
 * Roslynator download: https://marketplace.visualstudio.com/items?itemName=josefpihrt.Roslynator2019
 * Roslynator repo: https://github.com/JosefPihrt/Roslynator
 *
 * The .exe should be kept in resources/tools.
 */
public class LocTool implements IToolLOC {

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
    public LocTool(String name, Path toolsDirectory, Path msBuild) {
        this.toolsDirectory = toolsDirectory;
        this.msBuild = msBuild;
    }

    @Override
    public Integer analyze(Path path) {

        String sep = File.separator;
        ProcessBuilder pb;

        // strings for CLI call
        String tool = toolsDirectory.toAbsolutePath().toString() + sep + "Roslynator" + sep + "bin" + sep + "Roslynator.exe";
        String command = "loc";
        String msBuild = "--msbuild-path=" + this.msBuild.toString();
        String target = path.toString();

        // assert windows environment
        if(System.getProperty("os.name").contains("Windows")){
            pb = new ProcessBuilder("cmd.exe", "/c", tool, command, msBuild, target);
        } else {
            throw new RuntimeException("Roslynator C# analysis not supported on non-Windows machines.");
        }

        // run the cmd command
        Integer loc = null;
        Process proc = null;
        try {
            proc = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            // grab desired line for lines of code data
            String s;
            String locLoc = null;
            while ((s = stdInput.readLine()) != null) {
                if (s.contains("% lines of code")) {
                    locLoc = s;
                }
            }

            // parse the line of code integer
            assert locLoc != null;
            loc = new Integer(locLoc.substring(0, locLoc.indexOf(" ")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (loc == null) {
            throw new RuntimeException("LoC variable did not evaluate to a positive number");
        }
        return loc;
    }
}
