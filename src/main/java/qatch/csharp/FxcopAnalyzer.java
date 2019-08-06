package qatch.csharp;

import qatch.analysis.IAnalyzer;
import qatch.model.Property;
import qatch.model.PropertySet;
import qatch.utility.FileUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FxcopAnalyzer implements IAnalyzer {

    static final String TOOL_NAME = "FxCop";

    @Override
    public void analyze(File src, File dest, PropertySet properties) {

        Path assembly = setup(src);

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
                analyzeSubroutine(assembly, dest, p.getMeasure().getRulesetPath(), p.getName()+".xml");
            }
        }
    }

    /**
     * Analyze a single project against a certain ruleset (property) by calling the FxCop tool
     * through the command line with the appropriate configuration. Note that a project is the module a
     * .csproj file refers to. A C# solution (.sln) is a collection of multiple projects.
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
     * Finds the correct assembly for FxCop to analyze
     *
     * @param src the root directory to search for assemblies. A .csproj file should be in this directory
     * @return the path to the assembly to analyze
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
        // TODO: refactor into functional form, can merge this check into initial assemblyPaths addAll function
        for (Path p : assemblyPaths) {
            for (String directory : p.toString().split("\\\\")) {
                if (directory.trim().equals("obj") || directory.toLowerCase().contains("test")) {
                    removePaths.add(p);
                }
            }
        }
        assemblyPaths.removeAll(removePaths);

        /*
         * TODO: assemblyPaths can have multiple items if the same built assembly exists in Debug, Release, etc, folders.
         *  .NET does this automatically if there are multiple configurations set up and run in the build environment.
         *  Need to find a way to know which build folder to prioritize. This currently just picks the first one in the set
         */
        return assemblyPaths.iterator().next();

    }

}
