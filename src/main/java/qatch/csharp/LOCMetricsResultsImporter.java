package qatch.csharp;

import qatch.analysis.IMetricsResultsImporter;
import qatch.model.MetricSet;
import qatch.model.Metrics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This class is responsible for importing all the metrics that the LOCMetrics
 * tool calculates for a certain project, into a FindingSet object.
 *
 * Each object of the MetricSet contains all the metrics of a certain
 * class of the whole project.
 *
 * (TODO): class is very WIP, clean up and refactor soon
 */
public class LOCMetricsResultsImporter implements IMetricsResultsImporter {

    @Override
    public MetricSet parse(Path path) throws IOException {

        // This will change later. Currently using MetricSet to mirror the by-class approach used in original CKJM class
        MetricSet metricSet = new MetricSet();
        Metrics metrics = new Metrics();

        File file = new File(path.toString());
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        String[] headerRow = lines.get(0).split(",", -1);
        String[] totalsRow = lines.get(1).split(",", -1);

        metrics.setName(totalsRow[0]);
        metrics.setWmc(-1);
        metrics.setDit(-1);
        metrics.setNoc(-1);
        metrics.setCbo(-1);
        metrics.setRfc(-1);
        metrics.setLcom(-1);
        metrics.setCa(-1);
        metrics.setCe(-1);
        metrics.setNpm(-1);
        metrics.setLcom3(-1);
        metrics.setLoc(Integer.parseInt(totalsRow[2]));
        metrics.setDam(-1);
        metrics.setMoa(-1);
        metrics.setMfa(-1);
        metrics.setCam(-1);
        metrics.setIc(-1);
        metrics.setCbm(-1);
        metrics.setAmc(-1);
        metrics.setMethods(null);

        metricSet.addMetrics(metrics);
        return(metricSet);

    }

}
