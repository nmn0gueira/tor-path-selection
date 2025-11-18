package pt.unl.fct.pds;

import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.parser.ConsensusParser;
import pt.unl.fct.pds.parser.ServerDescriptorParser;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.path.TorPathSelection;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static pt.unl.fct.pds.utils.NetworkUtils.download;

/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 
{
    private static final String[] knownDirectoryAuthorities = new String[]{
            "217.196.147.77",
            "171.25.193.9:443",
            "216.218.219.41",
            "45.66.35.11"
    };

    private static final String CACHED_CONSENSUS = "cache/consensus";
    private static final String CACHED_SV_DESCRIPTORS = "cache/sv_descriptors";
    private static final String DEFAULT_TRAFFIC = "example/traffic";

    public static void main(String[] args) throws IOException {
        // Very simple arg parser
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argMap.put(args[i], args[i + 1]);
        }
        String consensusFile = argMap.get("--consensus");
        if (consensusFile == null) {
            consensusFile = CACHED_CONSENSUS;
            if (!(new File(CACHED_CONSENSUS).exists()))
                downloadConsensus();
        }
        String serverDescriptorsFile = argMap.get("--server-descriptors");
        if (serverDescriptorsFile == null) {
            serverDescriptorsFile = CACHED_SV_DESCRIPTORS;
            if (!(new File(CACHED_SV_DESCRIPTORS).exists()))
                downloadServerDescriptors();
        }

        String trafficFile = argMap.get("--traffic");

        System.out.println("Welcome to the Circuit Simulator!");

        ServerDescriptorParser serverDescriptorParser = new ServerDescriptorParser(serverDescriptorsFile);
        Map<String, Set<String>> nodeFamilies = serverDescriptorParser.parseServerDescriptors();
        ConsensusParser consensusParser = new ConsensusParser(consensusFile);
        List<Node> nodes = consensusParser.parseConsensus(nodeFamilies);


        // TODO: Implement different modes of execution from here on (tor, weighted, both?). Metrics can be ran by default and we do a run for each line in the traffic destinations
        PathSelection torPathSelection = new TorPathSelection(nodes);
        Circuit circuit = torPathSelection.buildCircuit(22);   // Replace this with actual port
        for (Node node : circuit.getNodes()) {
            System.out.println(node);
        }
        System.out.println("Circuit min bw: " + circuit.getMinBandwidth());
    }


    private static void downloadConsensus() {
        for (String da : knownDirectoryAuthorities) {
            try {
                download(
                        "http://" + da + "/tor/status-vote/current/consensus",
                        Paths.get(CACHED_CONSENSUS),
                        true,
                        bytes -> {
                            System.out.printf("Downloading consensus (%f MB)...\r", bytes * 1.0/(1024 * 1024));
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
                        Paths.get(CACHED_SV_DESCRIPTORS),
                        true,
                        bytes -> {
                            System.out.printf("Downloading server descriptors (%f MB)...\r", bytes * 1.0/(1024 * 1024));
                        });
                return;
            } catch (IOException ignored) {
            }
        }
        throw new RuntimeException("No download was possible!");
    }
}
