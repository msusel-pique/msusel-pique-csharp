package qatch.csharp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import qatch.analysis.IFindingsResultsImporter;
import qatch.model.Issue;
import qatch.model.IssueSet;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class FxcopResultsImporterTests {


    @Test
    public void testParse() throws IOException, SAXException, ParserConfigurationException {
        File locMetricsOutput = new File(getClass().getResource("/Eleflex_ReliabilityRules.xml").getPath());
        IFindingsResultsImporter fri = new FxcopResultsImporter();
        IssueSet is = fri.parse(locMetricsOutput.toPath());
        Issue i = is.get(0);

        Assert.assertEquals(is.getPropertyName(), "Eleflex_ReliabilityRules");
        Assert.assertEquals(i.getRuleName(), "AvoidCallingProblematicMethods");
        Assert.assertEquals(i.getRuleSetName(), "Microsoft.Reliability");
        Assert.assertEquals(i.getExternalInfoUrl(), "CA2001");
        Assert.assertEquals(i.getPriority(), 2);
    }

}
