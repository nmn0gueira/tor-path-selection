package pt.unl.fct.pds.parser;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TrafficParser {

    private String filename;
    private List<Integer> ports;

    public TrafficParser() {
    }

    public TrafficParser(String filename) {
        this.filename = filename;
        this.ports = new ArrayList<>();
    }

    public String getFilename() {
        return filename;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public List<Integer> parseTrafficFile() throws java.io.IOException {
        if (filename == null) {
            throw new IllegalStateException("Traffic filename is null");
        }
        java.nio.file.Path tf = Paths.get(filename);
        List<String> lines = Files.readAllLines(tf);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
                continue;
            try {
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    ports.add(Integer.parseInt(parts[parts.length - 1]));
                } else {
                    ports.add(Integer.parseInt(line));
                }
            } catch (NumberFormatException e) {
                System.err.println("Warning: could not parse traffic line '" + line + "'. Skipping.");
            }
        }
        if (ports.isEmpty()) {
            throw new IllegalStateException("No valid ports found in traffic file: " + filename);
        }
        return ports;
    }
}
