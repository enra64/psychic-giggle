package de.ovgu.softwareprojekt.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File handling utility functions
 */
public class FileUtils {
    /**
     * Read all contents of a file into a string
     *
     * @param path     file location
     * @param encoding expected encoding
     * @return a string representing the file content
     * @throws IOException if the file could not be found or read
     */
    @SuppressWarnings("SameParameterValue")
    public static String readFile(String path, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
