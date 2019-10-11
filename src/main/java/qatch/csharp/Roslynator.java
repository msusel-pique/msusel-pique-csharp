package qatch.csharp;

import qatch.analysis.AnalylsisResult;
import qatch.analysis.ITool;
import qatch.analysis.Tool;

import java.nio.file.Path;

/**
 * ITool implementation static analyasis tool class.
 *
 * Roslynator download: https://marketplace.visualstudio.com/items?itemName=josefpihrt.Roslynator2019
 * Roslynator repo: https://github.com/JosefPihrt/Roslynator
 * Roslynator is a collection of C# static analysis tools that can be run on .NET core, .NET framework,
 * and .NET standard projects so long as a solution (.sln) file exists.
 * Roslynator is able to generate both static analysis findings and loc/lloc metrics.
 *
 * The .exe should be kept in resources/tools.
 */
public class Roslynator extends Tool implements ITool {

    private Path toolsDirectory;

    public Roslynator(Path toolsDirectory) {
        this.setName("Roslynator");
        this.toolsDirectory = toolsDirectory;
    }

    @Override
    public Path analyze(Path path) {
        return null;
    }

    @Override
    public AnalylsisResult parse(Path path) {
        return null;
    }
}
