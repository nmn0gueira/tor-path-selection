package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.Cache;
import pt.unl.fct.pds.utils.RandomCollection;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public abstract class AbstractPathSelection implements PathSelection {

    protected int id;
    protected final List<Node> nodes;
    protected final Node[] guardSet;
    private static final Path CACHED_GUARD_SET = Cache.getCachePath("guard_set");

    protected AbstractPathSelection(List<Node> nodes) {
        this.nodes = nodes;
        this.guardSet = getGuardSet();
    }

    protected Node getExitNode(int destinationPort) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for  (Node node : nodes) {
            if (!node.getFlags().contains("Exit"))
                continue;
            if (!node.getFlags().contains("Fast"))
                continue;
            if (!node.satisfiesPolicy(destinationPort))
                continue;
            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }

    protected abstract Node getGuardNode(Node exitNode);

    protected abstract Node getMiddleNode(Node guardNode, Node exitNode);

    private Node[] getGuardSet() {
        if (Files.exists(CACHED_GUARD_SET)) {
            try (ObjectInputStream oi = new ObjectInputStream(Files.newInputStream(CACHED_GUARD_SET, StandardOpenOption.READ))) {
                Object object = oi.readObject();
                return (Node[]) object; // TODO: Maybe further check if cached guard set is valid no? Might not be necessary for the project ig
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            Node[] guardSet = new Node[3];
            RandomCollection<Node> suitableNodes = new RandomCollection<>();
            for (Node node : nodes) {
                if (!node.getFlags().contains("Guard"))
                    continue;
                if (!node.getFlags().contains("Running"))
                    continue;
                suitableNodes.add(node.getBandwidth(), node);
            }
            for (int i = 0; i < guardSet.length; i++) {
                guardSet[i] = suitableNodes.next(); // TODO: Should probably guarantee that the nodes we get here are all different (should be unlikely to happen tho)
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(CACHED_GUARD_SET,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE))) {
                out.writeObject(guardSet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return guardSet;
        }
    }

    @Override
    public Circuit buildCircuit(int destinationPort) {
        Node exitNode = getExitNode(destinationPort);
        Node guardNode = getGuardNode(exitNode);
        Node middleNode = getMiddleNode(guardNode, exitNode);
        Node[] circuitNodes = new Node[]{guardNode, middleNode, exitNode};
        return new Circuit(id, circuitNodes);
    }
}
