package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;

import java.util.List;

public class TorPathSelection extends AbstractPathSelection{

    public TorPathSelection(List<Node> nodes) {
        super(nodes);
        id = 1;
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
