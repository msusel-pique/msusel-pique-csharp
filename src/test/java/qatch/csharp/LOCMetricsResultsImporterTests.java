package qatch.csharp;

import org.junit.Assert;
import org.junit.Test;
import qatch.analysis.IMetricsResultsImporter;
import qatch.model.MetricSet;

import java.io.File;
import java.io.IOException;

public class LOCMetricsResultsImporterTests {

    @Test
    public void testParse() throws IOException {
        File locMetricsOutput = new File(getClass().getResource("/Eleflex_LocMetricsFolders.csv").getPath());
        IMetricsResultsImporter mri = new LOCMetricsResultsImporter();
        MetricSet ms = mri.parse(locMetricsOutput.toPath());

        Assert.assertEquals(ms.getMetricSet().get(0).getLoc(), 420);
    }

}
