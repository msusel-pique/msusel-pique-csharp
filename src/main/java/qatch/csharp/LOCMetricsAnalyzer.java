package qatch.csharp;

import qatch.analysis.IAnalyzer;
import qatch.model.PropertySet;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class LOCMetricsAnalyzer implements IAnalyzer {

    static final String RESULT_FILE_NAME = "LocMetricsFolders.csv";

    @Override
    public void analyze(File src, File dest, PropertySet properties) {

        ProcessBuilder pb;

        if(System.getProperty("os.name").contains("Windows")){
            pb = new ProcessBuilder(
                "cmd.exe", "/c",
                SingleProjectEvaluation.TOOLS_LOCATION + File.separator + "LocMetrics.exe", "-i", src.getAbsolutePath(), "-o", dest.getAbsolutePath()
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

        cleanAllButOne(new File(dest.getAbsolutePath()), RESULT_FILE_NAME);
    }

    private void cleanAllButOne(File directory, String toKeep) {
        for (File f : Objects.requireNonNull(directory.listFiles())) {
            if (!f.getName().equals(toKeep)) {
                f.delete();
            }
        }
    }

}
