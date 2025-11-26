package pt.unl.fct.pds;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.tukaani.xz.XZInputStream;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.path.TorPathSelection;
import pt.unl.fct.pds.path.WeightedPathSelection;

/**
 * Unit test for simple App.
 */
public class PathSelectionTest
    extends TestCase
{

    private static final List<Node> resourceNodes;

    static {
        try {
            resourceNodes = loadNodes("nodes.xz");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Node> loadNodes(String resourcePath) throws IOException, ClassNotFoundException {
        try (InputStream raw = PathSelectionTest.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            assertNotNull(raw);
            try (BufferedInputStream buf = new BufferedInputStream(raw);
                 XZInputStream xz = new XZInputStream(buf);
                 ObjectInputStream ois = new ObjectInputStream(xz)) {

                @SuppressWarnings("unchecked")
                List<Node> nodes = (LinkedList<Node>) ois.readObject();
                return nodes;
            }
        }
    }

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PathSelectionTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PathSelectionTest.class );
    }


    private void assertCircuit(Circuit circuit, int port) {
        assertNotNull(circuit);
        Node[] circuitNodes = circuit.getNodes();
        Node guardNode = circuitNodes[0];
        Node middleNode = circuitNodes[1];
        Node exitNode = circuitNodes[circuitNodes.length - 1];
        assertExitPolicy(exitNode, port);

        assertExitFlags(exitNode);
        assertGuardFlags(guardNode);
        assertMiddleFlags(middleNode);

        assertAllDifferent(circuitNodes);
        assertNotSameFamily(circuitNodes);
        assertNotSame16Subnet(circuitNodes);
    }

    // TODO: Missing unit tests for guard set

    private void assertAllDifferent(Node[] nodes) {
        assertNotNull(nodes);
        for (int i = 0; i < nodes.length - 1; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                assertNotSame(nodes[i], nodes[j]);
            }
        }
    }

    private void assertGuardFlags(Node guard) {
        assertNotNull(guard);
        assertTrue(guard.getFlags().contains("Running") && guard.getFlags().contains("Guard"));
    }

    private void assertMiddleFlags(Node middle) {
        assertNotNull(middle);
        assertTrue(middle.getFlags().contains("Fast"));
    }

    private void assertExitPolicy(Node exit, int port) {
        assertNotNull(exit);
        assertTrue(exit.satisfiesPolicy(port));
    }

    private void assertExitFlags(Node exit) {
        assertNotNull(exit);
        assertTrue(exit.getFlags().contains("Fast") && exit.getFlags().contains("Exit"));
    }

    private void assertNotSameFamily(Node[] nodes) {
        assertNotNull(nodes);
        for (int i = 0; i < nodes.length - 1; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                assertFalse(nodes[i].isInSameFamily(nodes[j]));
            }
        }
    }

    private void assertNotSame16Subnet(Node[] nodes) {
        assertNotNull(nodes);
        for (int i = 0; i < nodes.length - 1; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                assertFalse(nodes[i].isInSame16Subnet(nodes[j]));
            }
        }
    }

    private void testPathSelection(PathSelection pathSelection) {
        List<Integer> ports = new ArrayList<>(65535);
        for (int i = 0; i < 65535; i++) {
            ports.add(i);
        }
        ports.parallelStream()
                .forEach(port -> assertCircuit(pathSelection.buildCircuit(port), port));
    }

    public void testTorPathSelection() {
        PathSelection torPathSelection = new TorPathSelection(resourceNodes);
        testPathSelection(torPathSelection);

    }

    public void testWeightedPathSelection() {
        PathSelection weightedPathSelection = new WeightedPathSelection(resourceNodes, 0.5, 0.5);
        testPathSelection(weightedPathSelection);
    }
}
