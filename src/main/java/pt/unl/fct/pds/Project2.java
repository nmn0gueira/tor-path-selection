package pt.unl.fct.pds;

import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.parser.ConsensusParser;
import pt.unl.fct.pds.parser.ServerDescriptorParser;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.path.TorPathSelection;
import pt.unl.fct.pds.path.WeightedPathSelection;
import pt.unl.fct.pds.parser.TrafficParser;
import pt.unl.fct.pds.utils.Cache;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static pt.unl.fct.pds.utils.NetworkUtils.download;

/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 {
    private static final String[] knownDirectoryAuthorities = new String[] {
            "217.196.147.77",
            "171.25.193.9:443",
            "216.218.219.41",
            "45.66.35.11"
    };

    private static final Path CACHED_CONSENSUS = Cache.getCachePath("consensus");
    private static final Path CACHED_SV_DESCRIPTORS = Cache.getCachePath("sv_descriptors");
    private static final String DEFAULT_TRAFFIC = "example/traffic";

    public static void main(String[] args) throws IOException {
        // Very simple arg parser
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argMap.put(args[i], args[i + 1]);
        }
        String consensusFile = argMap.get("--consensus");
        if (consensusFile == null) {
            consensusFile = CACHED_CONSENSUS.toString();
            if (!Files.exists(CACHED_CONSENSUS))
                downloadConsensus();
        }
        String serverDescriptorsFile = argMap.get("--server-descriptors");
        if (serverDescriptorsFile == null) {
            serverDescriptorsFile = CACHED_SV_DESCRIPTORS.toString();
            if (!Files.exists(CACHED_SV_DESCRIPTORS))
                downloadServerDescriptors();
        }

        String trafficFile = argMap.getOrDefault("--traffic", DEFAULT_TRAFFIC);

        System.out.println("Welcome to the Circuit Simulator!");

        ServerDescriptorParser serverDescriptorParser = new ServerDescriptorParser(serverDescriptorsFile);
        Map<String, Set<String>> nodeFamilies = serverDescriptorParser.parseServerDescriptors();
        ConsensusParser consensusParser = new ConsensusParser(consensusFile);
        List<Node> nodes = consensusParser.parseConsensus(nodeFamilies);
        TrafficParser trafficParser = new TrafficParser(trafficFile);
        List<Integer> ports = trafficParser.parseTrafficFile();


        String mode = argMap.getOrDefault("--mode", "both"); // tor | weighted | both
        int runs = Integer.parseInt(argMap.getOrDefault("--runs", "1"));
        double alpha = Double.parseDouble(argMap.getOrDefault("--alpha", "0.5"));
        double beta = Double.parseDouble(argMap.getOrDefault("--beta", "0.5"));        

        if (mode.equals("tor") || mode.equals("both")) {

            PathSelection torPathSelection = new TorPathSelection(nodes);
            System.out.println("Running TorPathSelection metrics...");
            runMetrics("TorPathSelection", torPathSelection, ports, runs);
        }
        if (mode.equals("weighted") || mode.equals("both")) {

            PathSelection weightedPathSelection = new WeightedPathSelection(nodes, alpha, beta);
            System.out.println("Running WeightedPathSelection metrics (alpha=" + alpha + ", beta=" + beta + ")...");
            runMetrics("WeightedPathSelection", weightedPathSelection, ports, runs);
        }
    }

    private static void runMetrics(String name, PathSelection ps, List<Integer> ports, int runs) {

        Set<String> uniqueNodes = new HashSet<>();
        Set<String> guardSet = new HashSet<>();
        Set<String> middleSet = new HashSet<>();
        Set<String> exitSet = new HashSet<>();

        int total = 0;

        for (int r = 0; r < runs; r++) {
            for (int port : ports) {
                System.out.printf("%s: run %d/%d, port %d\r", name, r + 1, runs, port);
                Circuit c = ps.buildCircuit(port);
                total++;
                try {
                    Node[] nodesArr = c.getNodes();
                    if (nodesArr != null) {
                        for (Node n : nodesArr) {
                            if (n != null && n.getFingerprint() != null)
                                uniqueNodes.add(n.getFingerprint());
                        }
                        if (nodesArr.length > 0 && nodesArr[0] != null && nodesArr[0].getFingerprint() != null)
                            guardSet.add(nodesArr[0].getFingerprint());
                        if (nodesArr.length > 1 && nodesArr[1] != null && nodesArr[1].getFingerprint() != null)
                            middleSet.add(nodesArr[1].getFingerprint());
                        if (nodesArr.length > 2 && nodesArr[2] != null && nodesArr[2].getFingerprint() != null)
                            exitSet.add(nodesArr[2].getFingerprint());
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (total == 0) {
            System.out.println(name + ": no successful circuits generated.");
            return;
        }

        System.out.println("--- Metrics for " + name + " ---");
        System.out.println("Runs: " + runs + ", Ports tested: " + ports.size() + ", Total circuits: " + total);
        System.out.println("Unique nodes chosen (total): " + uniqueNodes.size());
        System.out.println("Unique per-position: guards=" + guardSet.size() + ", middle=" + middleSet.size() + ", exit=" + exitSet.size());
        System.out.println();

    }

    private static void downloadConsensus() {
        for (String da : knownDirectoryAuthorities) {
            try {
                download(
                        "http://" + da + "/tor/status-vote/current/consensus",
                        CACHED_CONSENSUS,
                        true,
                        bytes -> {
                            System.out.printf("Downloading consensus (%f MB)...\r", bytes * 1.0 / (1024 * 1024));
                        });
                return;
            } catch (IOException ignored) {

            }
        }
        throw new RuntimeException("No download was possible!");
    }

    private static void downloadServerDescriptors() {
        for (String da : knownDirectoryAuthorities) {
            try {
                download(
                        "http://" + da + "/tor/server/all",
                        CACHED_SV_DESCRIPTORS,
                        true,
                        bytes -> {
                            System.out.printf("Downloading server descriptors (%f MB)...\r",
                                    bytes * 1.0 / (1024 * 1024));
                        });
                return;
            } catch (IOException ignored) {
            }
        }
        throw new RuntimeException("No download was possible!");
    }
}
