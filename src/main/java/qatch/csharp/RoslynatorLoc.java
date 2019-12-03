package qatch.csharp;

import qatch.analysis.Diagnostic;
import qatch.analysis.IToolLOC;
import qatch.utility.FileUtility;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IToolLOC implementation using Roslynator CLI
 *
 * Roslynator download: https://marketplace.visualstudio.com/items?itemName=josefpihrt.Roslynator2019
 * Roslynator repo: https://github.com/JosefPihrt/Roslynator
 *
 * The .exe should be kept in resources/tools.
 */
public class RoslynatorLoc extends RoslynatorTool implements IToolLOC {

    private Path msBuild;

    /**
     * Constructor.
     * Roslynator analsis needs the MSBuild.exe path
     * (e.g. "C:/Program Files (x86)/Microsoft Visual Studio/2019/Community/MSBuild/Current/Bin")
     *
     * @param toolRoot
     *      *      Qatch-csharp tools directory location
     * @param msBuild
     *      Path to Bin folder containing MSBuild.exe
     */
    public RoslynatorLoc(Path toolRoot, Path msBuild) {
        super("Roslynator", toolRoot);
        this.msBuild = msBuild;
    }

    @Override
    public Path analyze(Path path) {
        throw new NotImplementedException();
    }

    @Override
    public Integer analyzeLinesOfCode(Path path) {

        path = path.toAbsolutePath();
        String sep = File.separator;
        ProcessBuilder pb;

        // Append .sln or .csproj file to path
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
        String tool = getExecutable().toAbsolutePath().toString();
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
                System.out.println("roslynator LoC: " + s);
                if (s.contains("% lines of code")) {
                    locLoc = s;
                }
            }

            // parse the line of code integer
            assert locLoc != null;
            Pattern p = Pattern.compile("\\d*,*\\d+");
            Matcher m = p.matcher(locLoc);
            if (m.find()) {
                loc = Integer.parseInt(m.group().replaceAll(",", ""));
            }
            else throw new RuntimeException("LoC expected output from tool was not found by regex");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (loc == null) {
            throw new RuntimeException("LoC variable did not evaluate to a positive number");
        }
        return loc;
    }

    @Override
    public Path initialize(Path path) {
        return roslynatorInitializeToTempFolder();
    }

    @Override
    public Map<String, Diagnostic> parseAnalysis(Path path) {
        throw new NotImplementedException();
    }
}
