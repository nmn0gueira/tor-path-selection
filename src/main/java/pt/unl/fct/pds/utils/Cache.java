package pt.unl.fct.pds.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Cache {

    private static final Path CACHE = Paths.get("cache");

    public static Path getCachePath(String filePath) {
        return CACHE.resolve(filePath);
    }
}
