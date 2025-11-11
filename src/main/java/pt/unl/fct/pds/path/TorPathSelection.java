package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.GeneralUtils;
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
            if (GeneralUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress()))
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
            // Might not need to verify both addresses since the subnet for both the guard and exit should be the same already
            // TODO: Also filter nodes of the same family
            if (GeneralUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress())
            && GeneralUtils.same16Subnet(node.getIpAddress(), guardNode.getIpAddress()))
                continue;

            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }
}
