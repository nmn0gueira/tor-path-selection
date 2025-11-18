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

    @Override
    public Node getGuardNode(Node exitNode) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for  (Node node : nodes) {
            if (!node.getFlags().contains("Guard"))
                continue;

            // TODO: Also filter nodes of the same family
            if (NetworkUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress()))
                continue;

            // TODO: Prioritize relays from persistent SAMPLED_GUARDS and CONFIRMED_GUARDS sets ??????
            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }

    @Override
    public Node getMiddleNode(Node guardNode, Node exitNode) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for  (Node node : nodes) {
            if (!node.getFlags().contains("Fast"))
                continue;
            // TODO: Also filter nodes of the same family
            if (NetworkUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress()))
                continue;

            assert !NetworkUtils.same16Subnet(node.getIpAddress(), guardNode.getIpAddress());   // This check should be be needed by transitivity so we assert instead

            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }
}
