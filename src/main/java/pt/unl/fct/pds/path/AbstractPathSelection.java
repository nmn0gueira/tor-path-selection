package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.path.guard.FileGuardSetStore;
import pt.unl.fct.pds.path.guard.GuardSetStore;
import pt.unl.fct.pds.utils.RandomCollection;

import java.io.*;
import java.util.List;

public abstract class AbstractPathSelection implements PathSelection {

    protected int id;
    protected final List<Node> nodes;
    protected final Node[] guardSet;

    protected AbstractPathSelection(List<Node> nodes, GuardSetStore guardSetStore) {
        this.nodes = nodes;
        this.guardSet = guardSetStore.getGuardSet();
    }

    protected AbstractPathSelection(List<Node> nodes) {
        this.nodes = nodes;
        this.guardSet = new FileGuardSetStore(nodes).getGuardSet();
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

    @Override
    public Circuit buildCircuit(int destinationPort) {
        Node exitNode = getExitNode(destinationPort);
        Node guardNode = getGuardNode(exitNode);
        Node middleNode = getMiddleNode(guardNode, exitNode);
        Node[] circuitNodes = new Node[]{guardNode, middleNode, exitNode};
        return new Circuit(id, circuitNodes);
    }
}
