package pique.csharp.evaluation;

import pique.evaluation.INormalizer;
import pique.model.Diagnostic;

public class NoNormalizer implements INormalizer {
    @Override
    public double normalize(double inValue, Diagnostic diagnostic) {
        return inValue;
    }

    @Override
    public String getNormalizerDiagnosticName() {
        return "not relevant diagnostic";
    }

    @Override
    public String getNormalizerName() {
        return "pique.csharp.evaluation.NoNormalizer";
    }
}
