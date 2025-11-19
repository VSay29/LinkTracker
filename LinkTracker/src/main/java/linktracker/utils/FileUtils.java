package linktracker.utils;

import linktracker.model.WebPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<WebPage> loadPages(Path file) throws IOException {
        List<WebPage> wps = new ArrayList<>();
        WebPage wp = null;
        for(String line : Files.readAllLines(file)){
            String[] split = line.split(";");
            wp = new WebPage(split[0], split[1]);
            wps.add(wp);
        }
        return wps;
    }

}
