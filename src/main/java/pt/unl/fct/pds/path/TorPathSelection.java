package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.RandomCollection;

import java.util.List;

public class TorPathSelection extends AbstractPathSelection{

    public TorPathSelection(List<Node> nodes) {
        super(nodes);
        id = 1;
    }

    @Override
    public Node getGuardNode(Node exitNode) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for (Node node : guardSet) {
            if (node == null)
                continue;
            if (!node.getFlags().contains("Guard"))
                continue;
            if (node.isInSameFamily(exitNode))
                continue;
            if (node.isInSame16Subnet(exitNode))
                continue;
            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }

    @Override
    public Node getMiddleNode(Node guardNode, Node exitNode) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for (Node node : nodes) {
            if (node == null)
                continue;
            if (!node.getFlags().contains("Fast"))
                continue;
            if (node.isInSameFamily(exitNode) || node.isInSameFamily(guardNode))
                continue;
            if (node.isInSame16Subnet(exitNode) || node.isInSame16Subnet(guardNode))
                continue;
            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }
}
