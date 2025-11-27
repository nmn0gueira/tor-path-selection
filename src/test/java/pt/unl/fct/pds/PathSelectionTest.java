package pt.unl.fct.pds;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.*;

import org.tukaani.xz.XZInputStream;
import pt.unl.fct.pds.path.PathSelection;
import pt.unl.fct.pds.path.TorPathSelection;
import pt.unl.fct.pds.path.WeightedPathSelection;
import pt.unl.fct.pds.path.guard.GuardSetStore;

/**
 * Unit test for simple App.
 */
public class PathSelectionTest
    extends TestCase
{

    static class TestGuardSetStore implements GuardSetStore {
        private final Node[] guardSet;
        public TestGuardSetStore(Node[] guardSet) {
            this.guardSet = guardSet;
        }

        public TestGuardSetStore(List<Node> nodes) {
            this.guardSet = GuardSetStore.createGuardSet(nodes);
        }

        @Override
        public Node[] getGuardSet() {
            return guardSet;
        }
    }

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


    private void assertCircuit(Circuit circuit, int port, Set<Node> guardSet) {
        assertNotNull(circuit);
        Node[] circuitNodes = circuit.getNodes();
        Node guardNode = circuitNodes[0];
        Node middleNode = circuitNodes[1];
        Node exitNode = circuitNodes[circuitNodes.length - 1];

        assertGuardInGuardSet(guardNode, guardSet);
        assertExitPolicy(exitNode, port);

        assertExitFlags(exitNode);
        assertGuardFlags(guardNode);
        assertMiddleFlags(middleNode);

        assertAllDifferent(circuitNodes);
        assertNotSameFamily(circuitNodes);
        assertNotSame16Subnet(circuitNodes);
    }

    private void assertGuardInGuardSet(Node guard, Set<Node> guardSet) {
        assertTrue(guardSet.contains(guard));
    }

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
                try {
                    assertFalse(nodes[i].isInSameFamily(nodes[j]));
                } catch (AssertionFailedError e) {
                    System.out.println("Node " + nodes[i] + " is in same family");
                    System.out.println("Node " + nodes[j] + " is in same family");
                    throw new RuntimeException(e);
                }
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

    private void testPathSelection(PathSelection pathSelection, Node[] guardSet) {
        List<Integer> ports = new ArrayList<>(65535);
        for (int i = 0; i < 65535; i++) {
            ports.add(i);
        }
        Set<Node> guardHashSet = new HashSet<>(Arrays.asList(guardSet));
        ports.parallelStream()
                .forEach(port -> assertCircuit(pathSelection.buildCircuit(port), port, guardHashSet));
    }

    public void testTorPathSelection() {
        GuardSetStore guardSetStore = new TestGuardSetStore(resourceNodes);
        PathSelection torPathSelection = new TorPathSelection(resourceNodes, guardSetStore);
        testPathSelection(torPathSelection, guardSetStore.getGuardSet());

    }

    public void testWeightedPathSelection() {
        GuardSetStore guardSetStore = new TestGuardSetStore(resourceNodes);
        PathSelection weightedPathSelection = new WeightedPathSelection(resourceNodes, guardSetStore, 0.5, 0.5);
        testPathSelection(weightedPathSelection, guardSetStore.getGuardSet());
    }
}
