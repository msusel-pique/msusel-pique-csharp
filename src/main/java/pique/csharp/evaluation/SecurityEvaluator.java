package pique.csharp.evaluation;

import pique.evaluation.IEvaluator;
import pique.model.Finding;

import java.util.Set;
import java.util.function.Function;

public class SecurityEvaluator implements IEvaluator {

    @Override
    public Function<Set<Finding>, Double> evalStrategy() {
        return findings -> {
            if (findings.isEmpty()) {
                return 1.0;
            }
            else {
                return 0.0;
            }
        };
    }

    @Override
    public String getName() {
        return "pique.csharp.evaluation.SecurityEvaluator";
    }
}
