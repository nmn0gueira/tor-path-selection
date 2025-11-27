package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.path.guard.GuardSetStore;
import pt.unl.fct.pds.utils.RandomCollection;

import java.util.List;

public class WeightedPathSelection extends AbstractPathSelection {

    private final double alpha;
    private final double beta;

    public WeightedPathSelection(List<Node> nodes, double alpha, double beta) {
        super(nodes);
        id = 2;
        this.alpha = alpha;
        this.beta = beta;
    }

    public WeightedPathSelection(List<Node> nodes, GuardSetStore guardSetStore, double alpha, double beta) {
        super(nodes, guardSetStore);
        id = 2;
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    protected Node getGuardNode(Node exitNode) {
        RandomCollection<Node> rc = new RandomCollection<>();
        for (Node node : guardSet) {
            if (node == null || node == exitNode)   // Using == here is intentional
                continue;
            if (!node.getFlags().contains("Guard"))
                continue;
            if (node.isInSameFamily(exitNode))
                continue;
            if (node.isInSame16Subnet(exitNode))
                continue;

            double w = node.getBandwidth();
            if (!node.getCountry().equals(exitNode.getCountry()))
                w *= (1 + alpha);

            if (w > 0)
                rc.add(w, node);
        }
        return rc.next();
    }

    @Override
    protected Node getMiddleNode(Node guardNode, Node exitNode) {
        RandomCollection<Node> rc = new RandomCollection<>();
        for (Node node : nodes) {
            if (node == null || node == guardNode || node == exitNode) // Using == here is intentional
                continue;
            if (!node.getFlags().contains("Fast"))
                continue;
            if (node.isInSameFamily(exitNode) || node.isInSameFamily(guardNode))
                continue;
            if (node.isInSame16Subnet(exitNode) || node.isInSame16Subnet(guardNode))
                continue;
            double w = node.getBandwidth();

            int shared = 0;
            if (node.getCountry().equals(guardNode.getCountry()))
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
