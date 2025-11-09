package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.RandomCollection;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPathSelection implements PathSelection {

    protected int id;
    protected final List<Node> nodes;

    protected AbstractPathSelection(List<Node> nodes) {
        this.nodes = nodes;
    }

    protected Node getExitNode() {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for  (Node node : nodes) {
            if (!node.getFlags().contains("Fast"))
                continue;
            // TODO: Missing filter on suitable exit policy. How tf do we evaluate that if no client traffic exists in this project for us to evaluate?
            // Maybe allow passing the kind of required traffic as an argument with default value as well
            if (false)
                continue;
            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }

    protected abstract Node getGuardNode(Node exitNode);

    protected abstract Node getMiddleNode(Node guardNode, Node exitNode);

    @Override
    public Circuit buildCircuit() {
        Node exitNode = getExitNode();
        Node guardNode = getGuardNode(exitNode);
        Node middleNode = getMiddleNode(guardNode, exitNode);
        Node[] circuitNodes = new Node[]{guardNode, middleNode, exitNode};
        return new Circuit(id, circuitNodes);
    }
}
