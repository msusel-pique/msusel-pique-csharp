package qatch.csharp;

import org.apache.commons.io.FilenameUtils;
import qatch.analysis.IToolLOC;
import qatch.utility.FileUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

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

    /**
     * LocTool analyze procedure.  Assumes that path points to the root directory of the project.
     * This LOC tool needs its target to point to either a .sln or .csproj file, so this method handles
     * appending the extra path part to its target.
     *
     * @param path
     *      Path to root directory of CSharp project
     * @return
     *      Integer lines of code value of either the .sln or .csproj in the root directory.
     */
    @Override
    public Integer analyze(Path path) {

        String sep = File.separator;
        ProcessBuilder pb;

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
        String tool = toolsDirectory.toAbsolutePath().toString() + sep + "Roslynator" + sep + "bin" + sep + "Roslynator.exe";
        String command = "loc";
        String msBuild = "--msbuild-path=" + this.msBuild.toString();
        String target = path.toString();

        // Assert windows environment
        if(System.getProperty("os.name").contains("Windows")){
            pb = new ProcessBuilder("cmd.exe", "/c", tool, command, msBuild, target);
        } else {
            throw new RuntimeException("Roslynator C# analysis not supported on non-Windows machines.");
        }

        // Run the cmd command
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
