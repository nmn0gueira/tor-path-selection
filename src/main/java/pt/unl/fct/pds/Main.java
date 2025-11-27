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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

import static pt.unl.fct.pds.utils.NetworkUtils.download;

/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Main {
    private static final String[] knownDirectoryAuthorities = new String[] {
            "217.196.147.77",
            "171.25.193.9:443",
            "216.218.219.41",
            "45.66.35.11"
    };

    private static final Path CACHED_CONSENSUS = Cache.getCachePath("consensus");
    private static final Path CACHED_SV_DESCRIPTORS = Cache.getCachePath("sv_descriptors");
    private static final String DEFAULT_TRAFFIC = "example/IPAddresses.txt";

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

        Map<String, Set<String>> nodeFamilies = new ServerDescriptorParser(serverDescriptorsFile).parseServerDescriptors();
        List<Node> nodes = new ConsensusParser(consensusFile).parseConsensus(nodeFamilies);
        List<Integer> ports = new TrafficParser(trafficFile).parseTrafficFile();

        String mode = argMap.getOrDefault("--mode", "both"); // tor | weighted | both
        int runs = Integer.parseInt(argMap.getOrDefault("--runs", "1"));
        double alpha = Double.parseDouble(argMap.getOrDefault("--alpha", "0.5"));
        double beta = Double.parseDouble(argMap.getOrDefault("--beta", "0.5"));

        if (mode.equals("tor") || mode.equals("both")) {
            PathSelection torPathSelection = new TorPathSelection(nodes);
            System.out.println("Running TorPathSelection metrics...");
            runMetrics(torPathSelection, ports, runs);
        }
        if (mode.equals("weighted") || mode.equals("both")) {
            PathSelection weightedPathSelection = new WeightedPathSelection(nodes, alpha, beta);
            System.out.println("Running WeightedPathSelection metrics (alpha=" + alpha + ", beta=" + beta + ")...");
            runMetrics(weightedPathSelection, ports, runs);
        }
    }

    private static void runMetrics(PathSelection ps, List<Integer> ports, int runs) {

        List<Integer> minBws = new ArrayList<>(ports.size() * runs);
        int totalCircuits = runs * ports.size();

        Map<String, Integer> globalNodeCounts = new HashMap<>();
        Map<String, Integer> guardNodeCounts = new HashMap<>();
        Map<String, Integer> middleNodeCounts = new HashMap<>();
        Map<String, Integer> exitNodeCounts = new HashMap<>();

        Map<String, Integer> globalCountryCounts = new HashMap<>();
        Map<String, Integer> guardCountryCounts = new HashMap<>();
        Map<String, Integer> middleCountryCounts = new HashMap<>();
        Map<String, Integer> exitCountryCounts = new HashMap<>();

        for (int r = 0; r < runs; r++) {
            int innerLoopStart = r * ports.size();
            for (int i = 0; i < ports.size(); i++) {
                int port = ports.get(i);
                System.out.printf("Port (%d) (%d/%d)...\r", port, innerLoopStart + i + 1, totalCircuits);
                Circuit c = ps.buildCircuit(port);
                minBws.add(c.getMinBandwidth());
                Node[] nodesArr = c.getNodes();
                if (nodesArr != null) {
                    for (int pos = 0; pos < nodesArr.length; pos++) {
                        Node n = nodesArr[pos];
                        if (n == null || n.getFingerprint() == null)
                            continue;
                        String fp = n.getFingerprint();
                        String country = n.getCountry();
                        globalNodeCounts.put(fp, globalNodeCounts.getOrDefault(fp, 0) + 1);
                        globalCountryCounts.put(country, globalCountryCounts.getOrDefault(country, 0) + 1);
                        if (pos == 0) {
                            guardNodeCounts.put(fp, guardNodeCounts.getOrDefault(fp, 0) + 1);
                            guardCountryCounts.put(country, guardCountryCounts.getOrDefault(fp, 0) + 1);
                        } else if (pos == 1) {
                            middleNodeCounts.put(fp, middleNodeCounts.getOrDefault(fp, 0) + 1);
                            middleCountryCounts.put(country, middleCountryCounts.getOrDefault(country, 0) + 1);
                        } else if (pos == 2) {
                            exitNodeCounts.put(fp, exitNodeCounts.getOrDefault(fp, 0) + 1);
                            exitCountryCounts.put(country, exitCountryCounts.getOrDefault(country, 0) + 1);
                        }
                    }
                }
            }
        }

        double avg = minBws.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int min = minBws.stream().mapToInt(Integer::intValue).min().orElse(0);
        int max = minBws.stream().mapToInt(Integer::intValue).max().orElse(0);
        Collections.sort(minBws);
        double median;
        int mid = minBws.size() / 2;
        if (minBws.size() % 2 == 0)
            median = (minBws.get(mid - 1) + minBws.get(mid)) / 2.0;
        else
            median = minBws.get(mid);

        int tenthPercentile = minBws.get(minBws.size() / 10);

        double globalNodeEntropy = computeShannonEntropy(globalNodeCounts, totalCircuits * 3);
        double guardNodeEntropy = computeShannonEntropy(guardNodeCounts, totalCircuits);
        double middleNodeEntropy = computeShannonEntropy(middleNodeCounts, totalCircuits);
        double exitNodeEntropy = computeShannonEntropy(exitNodeCounts, totalCircuits);

        double globalCountryEntropy = computeShannonEntropy(globalCountryCounts, totalCircuits * 3);
        double guardCountryEntropy = computeShannonEntropy(guardCountryCounts, totalCircuits);
        double middleCountryEntropy = computeShannonEntropy(middleCountryCounts, totalCircuits);
        double exitCountryEntropy = computeShannonEntropy(exitCountryCounts, totalCircuits);

        System.out.println("--- Metrics for " + ps.getClass().getSimpleName() + " ---");
        System.out.println("Runs: " + runs + ", Ports tested: " + ports.size() + ", Total circuits: " + totalCircuits);
        System.out.println("Unique nodes chosen (total): " + globalNodeCounts.size());
        System.out.println("Unique nodes per-position: guards=" + guardNodeCounts.size() + ", middle=" + middleNodeCounts.size()
                + ", exit=" + exitNodeCounts.size());
        System.out.println("Unique countries chosen (total): " + globalCountryCounts.size());
        System.out.println("Unique countries per-position: guards=" + guardCountryCounts.size() + ", middle=" + middleCountryCounts.size()
                + ", exit=" + exitCountryCounts.size());
        System.out.printf("(Node) Shannon Entropy (global): %.6f\n", globalNodeEntropy);
        System.out.printf("(Node) Shannon Entropy (guard/middle/exit): %.6f / %.6f / %.6f\n", guardNodeEntropy, middleNodeEntropy,
                exitNodeEntropy);
        System.out.printf("(Country) Shannon Entropy (global): %.6f\n", globalCountryEntropy);
        System.out.printf("(Country) Shannon Entropy (guard/middle/exit): %.6f / %.6f / %.6f\n", guardCountryEntropy, middleCountryEntropy,
                exitCountryEntropy);
        System.out.println("Minimum bandwidth average: " + String.format("%.2f", avg) + " (min=" + min + ", max=" + max + ", median="
                + median + ", 10th%= " + tenthPercentile  + ")");
        System.out.println();
    }

    private static double computeShannonEntropy(Map<String, Integer> counts, int totalSelections) {
        if (counts == null || counts.isEmpty() || totalSelections <= 0)
            return 0.0;
        double entropy = 0.0;
        for (int freq : counts.values()) {
            if (freq <= 0)
                continue;
            double p = (double) freq / (double) totalSelections;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
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
