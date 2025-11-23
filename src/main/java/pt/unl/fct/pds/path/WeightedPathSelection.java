package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.NetworkUtils;
import pt.unl.fct.pds.utils.RandomCollection;

import java.util.List;

public class WeightedPathSelection extends AbstractPathSelection {

    private double alpha;
    private double beta;

    public WeightedPathSelection(List<Node> nodes) {
        super(nodes);
        id = 2;
    }

    public WeightedPathSelection(List<Node> nodes, double alpha, double beta) {
        super(nodes);
        id = 2;
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public Node getGuardNode(Node exitNode) {
        RandomCollection<Node> rc = new RandomCollection<>();
        for (Node node : guardSet) {
            if (node == null)
                continue;

            if (!node.getFlags().contains("Guard"))
                continue;

            boolean mutualFamily = (node.getFamily().contains(exitNode.getFingerprint())
                    || node.getFamily().contains(exitNode.getNickname()))
                    && (exitNode.getFamily().contains(node.getFingerprint())
                            || exitNode.getFamily().contains(node.getNickname()));
            if (mutualFamily)
                continue;

            if (NetworkUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress()))
                continue;

            double w = node.getBandwidth();
            if (!node.getCountry().equals(exitNode.getCountry()))
                w *= (1 + alpha);

            rc.add(w, node);
        }
        return rc.next();
    }

    @Override
    public Node getMiddleNode(Node guardNode, Node exitNode) {
        RandomCollection<Node> rc = new RandomCollection<>();
        for (Node node : nodes) {

            if (node == null)
                continue;
            if (!node.getFlags().contains("Fast"))
                continue;
            
            boolean mutualFamily = (node.getFamily().contains(exitNode.getFingerprint())
                    || node.getFamily().contains(exitNode.getNickname()))
                    && (exitNode.getFamily().contains(node.getFingerprint())
                            || exitNode.getFamily().contains(node.getNickname()));
            if (mutualFamily)
                continue;
            if (NetworkUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress()))
                continue;
            double w = node.getBandwidth();

            int shared = 0;
            if (guardNode != null && node.getCountry().equals(guardNode.getCountry())) 
                shared++;
            if (node.getCountry().equals(exitNode.getCountry())) 
                shared++;

            double multiplier = 1 + (3 - shared) * beta;
            w *= multiplier;

            if (w > 0) 
                rc.add(w, node);
        }
        return rc.next();
    }

}
