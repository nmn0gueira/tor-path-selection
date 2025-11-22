package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.NetworkUtils;
import pt.unl.fct.pds.utils.RandomCollection;

import java.util.List;

public class TorPathSelection extends AbstractPathSelection{

    public TorPathSelection(List<Node> nodes) {
        super(nodes);
        id = 1;
    }

    /**
     * From <a href="https://spec.torproject.org/path-spec/path-selection-constraints.html?highlight=family#family-membership">...</a> (...) two relays belong to the same family if each relay lists the other relay in its family list.
     * @param exitNode previous exit node
     * @return sampled guard node
     */
    @Override
    public Node getGuardNode(Node exitNode) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for (Node node : guardSet) {
            if (!node.getFlags().contains("Guard"))
                continue;
            // If both nodes list each other in their family entries (either by fingerprint or nickname)
            if ((node.getFamily().contains(exitNode.getFingerprint()) || node.getFamily().contains(exitNode.getNickname()) &&
                    (exitNode.getFamily().contains(node.getFingerprint()) || exitNode.getFamily().contains(node.getNickname()))))
                continue;
            if (NetworkUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress()))
                continue;
            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }

    @Override
    public Node getMiddleNode(Node guardNode, Node exitNode) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for (Node node : nodes) {
            if (!node.getFlags().contains("Fast"))
                continue;
            // If both nodes list each other in their family entries (either by fingerprint or nickname)
            if ((node.getFamily().contains(exitNode.getFingerprint()) || node.getFamily().contains(exitNode.getNickname()) &&
                    (exitNode.getFamily().contains(node.getFingerprint()) || exitNode.getFamily().contains(node.getNickname()))))
                continue;
            if (NetworkUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress()))
                continue;

            // These check should not be needed by transitivity so we assert instead
            assert !NetworkUtils.same16Subnet(node.getIpAddress(), guardNode.getIpAddress());
            assert !(node.getFamily().contains(guardNode.getFingerprint()) || node.getFamily().contains(guardNode.getNickname()) &&
                    (guardNode.getFamily().contains(node.getFingerprint()) || guardNode.getFamily().contains(node.getNickname())));

            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }
}
