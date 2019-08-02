package qatch.csharp;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class Scratch {

    @Test
    public void scratch() {
        String s = "abcdTESTijkl";
        boolean b = s.toLowerCase().contains("test");
        System.out.println(b);
    }

    @Test
    public void testUserDirectory() throws IOException {
        File root = new File(System.getProperty("user.dir"));
        System.out.println(root.toString());
        System.out.println(root.getAbsolutePath());
        System.out.println(root.getCanonicalPath());
    }
}
