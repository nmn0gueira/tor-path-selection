package pt.unl.fct.pds;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.utils.ConsensusParser;

import java.io.IOException;
import java.util.List;


/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 
{
    private static final String DEFAULT_FILE = "example/consensus";

    public static void main( String[] args ) throws IOException {
        // Here we write our logic to choose circuits!
        System.out.println("Welcome to the Circuit Simulator!");

        ConsensusParser parser = new ConsensusParser(DEFAULT_FILE);
        List<Node> nodes = parser.parseConsensus();
    }
}
