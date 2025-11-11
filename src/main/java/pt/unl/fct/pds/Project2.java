package pt.unl.fct.pds;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.path.TorPathSelection;
import pt.unl.fct.pds.utils.ConsensusParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 
{
    private static final String DEFAULT_CONSENSUS = "example/consensus";
    private static final String DEFAULT_TRAFFIC = "example/traffic";

    public static void main(String[] args) throws IOException {

        // Very simple arg parser
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argMap.put(args[i], args[i + 1]);
        }

        String consensusFile = argMap.getOrDefault("--consensus", DEFAULT_CONSENSUS);
        String trafficFile = argMap.getOrDefault("--traffic", DEFAULT_TRAFFIC);

        System.out.println("Welcome to the Circuit Simulator!");

        ConsensusParser parser = new ConsensusParser(consensusFile);
        List<Node> nodes = parser.parseConsensus();

        /*
        for (Node node : nodes) {
            System.out.println(node.toString());
        }*/

        PathSelection torPathSelection = new TorPathSelection(nodes);
        Circuit circuit = torPathSelection.buildCircuit();
    }
}
