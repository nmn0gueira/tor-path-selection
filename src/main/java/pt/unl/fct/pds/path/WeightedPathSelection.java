package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;

import java.util.List;

public class WeightedPathSelection extends AbstractPathSelection{

    public WeightedPathSelection(List<Node> nodes) {
        super(nodes);
        id = 2;
    }


    @Override
    public Node getGuardNode(Node exitNode) {
        return null;
    }

    @Override
    public Node getMiddleNode(Node guardNode, Node exitNode) {
        return null;
    }
}
