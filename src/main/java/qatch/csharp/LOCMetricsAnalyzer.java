package qatch.csharp;

import qatch.analysis.IAnalyzer;
import qatch.model.PropertySet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class gathers metrics on a single project by invoking the
 * LOCMetrics.exe tool.
 *
 * Due to being only runnable on Windows, these methods should only
 * be used on C# projects
 */
public class LOCMetricsAnalyzer implements IAnalyzer {

    final static String RESULT_FILE_NAME = "LocMetricsFolders.csv";

    @Override
    public void analyze(Path src, Path dest, PropertySet properties) {

        ProcessBuilder pb;

        if(System.getProperty("os.name").contains("Windows")){
            pb = new ProcessBuilder(
                "cmd.exe", "/c",
                "\"" + SingleProjectEvaluation.TOOLS_LOCATION + File.separator + "LocMetrics.exe" + "\"",
                "-i",
                "\"" + src.toAbsolutePath().toString() + "\"",
                "-o",
                "\"" + dest.toAbsolutePath().toString() + "\""
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

        cleanAllButOne(dest, RESULT_FILE_NAME, src.getFileName().toString());
    }

    @Override
    public Path targetSrcDirectory(Path path) {
        return path;
    }

    /**
     * Filekeeping method. Removes unwanted files from LocMetrics run and renames the results file to a
     * project-specific name
     *
     * @param directory
     *      Directory containing the LocMetrics results
     * @param toKeep
     *      All files are removed except the file with this name
     * @param toRename
     *      Project-specific name to rename the kept file
     */
    private void cleanAllButOne(Path directory, String toKeep, String toRename) {
        Set<Path> toDelete = new HashSet<>();

        try {
            Files.walk(directory, 1)
                    .filter(p -> p.getFileName().toString().contains("LocMetrics"))
                    .filter(p -> !p.getFileName().toString().equalsIgnoreCase(toKeep))
                    .forEach(toDelete::add);
        } catch (IOException e) { e.printStackTrace(); }

        // delete unwanted files
        toDelete.forEach(p -> {
            try { Files.deleteIfExists(p); }
            catch (IOException e) { e.printStackTrace(); } });

        // rename remaining file to project-specific name
        File metrics = new File(directory.toFile(), toKeep);
        metrics.renameTo(new File(directory.toFile(), toRename + "_" + metrics.getName()));
    }
}
