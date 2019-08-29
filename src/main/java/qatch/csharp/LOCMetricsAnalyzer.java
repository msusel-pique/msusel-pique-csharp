package qatch.csharp;

import qatch.analysis.IAnalyzer;
import qatch.model.PropertySet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * This class gathers metrics on a single project by invoking the
 * LOCMetrics.exe tool.
 *
 * Due to being only runnable on Windows, these methods should only
 * be used on C# projects
 */
public class LOCMetricsAnalyzer implements IAnalyzer {

    final static String TOOL_RESULT_FILE_NAME = "LocMetricsFolders.csv";
    private Set<String> toKeep = new HashSet<>();
    private Path toolsDirectory;

    public LOCMetricsAnalyzer(Path toolsDirectory) {
        toKeep.add(TOOL_RESULT_FILE_NAME);
        this.toolsDirectory = toolsDirectory;
    }


    @Override
    public Path getToolsDirectory() {
        return toolsDirectory;
    }

    public void setToolsDirectory(Path toolsDirectory) { this.toolsDirectory = toolsDirectory; }



    @Override
    public void analyze(Path src, Path dest, PropertySet properties) {

        ProcessBuilder pb;
        if(System.getProperty("os.name").contains("Windows")){
            pb = new ProcessBuilder(
                    "cmd.exe", "/c",
                    toolsDirectory.toString() + File.separator + "LocMetrics.exe",
                    "-i",
                    src.toAbsolutePath().toString(),
                    "-o",
                    dest.toAbsolutePath().toString()
            );
        }
        else throw new RuntimeException("LOCMetrics tool only supported on Windows operating systems.");

        pb.redirectErrorStream(true);
        Process p = null;
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

        cleanAllButOne(dest, src.getFileName());
    }


    /**
     * Filekeeping method. Removes unwanted files from LocMetrics run and renames the results file to a
     * project-specific name
     *  @param directory
     *      Directory containing the LocMetrics results
     * @param newName
     *      Prefix of new name for metrics result file
     */
    private void cleanAllButOne(Path directory, Path newName) {
        Set<Path> toDelete = new HashSet<>();

        try {
            Files.walk(directory, 1)
                    .filter(p -> p.getFileName().toString().contains("LocMetrics"))
                    .filter(p -> !toKeep.contains(p.getFileName().toString()))
                    .forEach(toDelete::add);
        } catch (IOException e) { e.printStackTrace(); }

        // delete unwanted files
        toDelete.forEach(p -> {
            try { Files.deleteIfExists(p); }
            catch (IOException e) { e.printStackTrace(); } });

        // rename remaining file to project-specific name
        File metrics = new File(directory.toFile(), LOCMetricsAnalyzer.TOOL_RESULT_FILE_NAME);
        File renamedMetrics = new File(directory.toFile(), newName.toString() + "_" + metrics.getName());
        metrics.renameTo(renamedMetrics);

        // add to list of files to keep each analysis
        toKeep.add(renamedMetrics.getName());
    }
}
