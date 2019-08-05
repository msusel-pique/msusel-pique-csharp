package qatch.csharp;

import org.apache.commons.io.FileUtils;
import qatch.analysis.IAnalyzer;
import qatch.model.Property;
import qatch.model.PropertySet;
import qatch.utility.FileUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FxcopAnalyzer implements IAnalyzer {

    static final String TOOL_NAME = "FxCop";

    @Override
    public void analyze(File src, File dest, PropertySet properties) {

        Path assemblies = setup(src);

        //Create an Iterator in order to iterate through the properties of the desired PropertySet object
        Iterator<Property> iterator = properties.iterator();
        Property p;

        //For each property found in the PropertySet do...
        while(iterator.hasNext()) {

            //Get the current property
            p = iterator.next();

            //Check if it is an FxCop Property
            if (p.getMeasure().getTool().equals(FxcopAnalyzer.TOOL_NAME)) {
                //Analyze the project against this property
                analyzeSubroutine(assemblies, dest, p.getMeasure().getRulesetPath(), p.getName()+".xml");
            }
        }
    }

    /**
     * Analyze a single project against a certain ruleset (property) by calling the FxCop tool
     * through the command line with the appropriate configuration.
     *
     * @param src
     *      The path of the folder that contains the sources of the project. The folder must contain
     *      at least one .dll or .exe item (at any depth, a recursive search is performed).
     * @param dest
     *      The path where the tool results will be placed.
     * @param rulesetPath
     *      The  rules against which the project will be analyzed.
     * @param fileName
     *      The name of the XML file containing scan results.
     */
    private void analyzeSubroutine(Path src, File dest, String rulesetPath, String fileName) {
        String sep = File.separator;
        ProcessBuilder pb;
        String destFile = dest + sep + fileName;

        // begin building the strings to run the FxCop CLT
        String fxcop = SingleProjectEvaluation.TOOLS_LOCATION + sep + "FxCop" + sep + "FxCopCmd.exe";
        String assemblyDir = "/f:" + src.toAbsolutePath().toString();
        String destExt = "/out:" + destFile;
        String rulesetExt = "/r:" + rulesetPath;
        String fo = "/fo";

        if(System.getProperty("os.name").contains("Windows")){
            pb = new ProcessBuilder("cmd.exe", "/c", fxcop, assemblyDir, destExt, rulesetExt, fo);
        } else {
            throw new RuntimeException("FxCop C# analysis not supported on non-Windows machines. FxCopCmd.exe tool only supported on Windows.");
        }

        pb.redirectErrorStream(true);
        Process p = null;

        // run the tool
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            assert p != null;
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Fills a temporary directory will all propritary assemblies of a project
     * located at src
     *
     * @param src the root directory to search for assemblies
     * @return the path to the temporary directory holding the assemblies
     */
    private Path setup(File src) {

        Set<Path> assemblyPaths = new HashSet<>();
        Set<Path> removePaths = new HashSet<>();

        // only look for file names that match with .csproj file names. This causes external dependencies to be ignored
        Set<String> projectNames = FileUtility.findFileNamesFromExtension(src.toPath(), ".csproj");
        projectNames.forEach(p -> assemblyPaths.addAll(FileUtility.findAssemblies(src, p, ".exe", ".dll")));
        if (assemblyPaths.isEmpty()) {
            throw new RuntimeException("[ERROR] No directories containing .exe or .dll file(s) were found in project root "
                    + src + ". Has the project been built?");
        }

        // ignore found files that were in the obj folder or are tests
        // TODO: refactor into functional form
        for (Path p : assemblyPaths) {
            for (String directory : p.toString().split("\\\\")) {
                if (directory.trim().equals("obj") || directory.toLowerCase().contains("test")) {
                    removePaths.add(p);
                }
            }
        }
        assemblyPaths.removeAll(removePaths);

        // copy all binaries to be scanned into 1 temporary folder
        Path tempDir = new File( "temp_bin_dir").toPath();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        assemblyPaths.forEach(p -> {
            try {
                FileUtils.copyFileToDirectory(p.toFile(), tempDir.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // return the temporary folder containing only necessary files for analysis
        return tempDir;

    }

}
