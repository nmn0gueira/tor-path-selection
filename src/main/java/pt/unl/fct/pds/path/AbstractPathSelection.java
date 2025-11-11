package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.RandomCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPathSelection implements PathSelection {

    protected int id;
    protected final List<Node> nodes;

    protected AbstractPathSelection(List<Node> nodes) {
        this.nodes = nodes;
    }

    protected Node getExitNode(Set<Integer> requiredPorts) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for  (Node node : nodes) {
            if (!node.getFlags().contains("Exit"))
                continue;
            if (!node.getFlags().contains("Fast"))
                continue;
            if (!satisfiesPolicy(requiredPorts, node.getExitPolicy()))
                continue;
            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }

    protected abstract Node getGuardNode(Node exitNode);

    protected abstract Node getMiddleNode(Node guardNode, Node exitNode);

    @Override
    public Circuit buildCircuit() {
        Set<Integer> requiredPorts = new HashSet<>();   // TODO: Replace with proper traffic ports
        Node exitNode = getExitNode(requiredPorts);
        Node guardNode = getGuardNode(exitNode);
        Node middleNode = getMiddleNode(guardNode, exitNode);
        Node[] circuitNodes = new Node[]{guardNode, middleNode, exitNode};
        return new Circuit(id, circuitNodes);
    }

    /**
     * This snippet below may seem confusing. The policy satisfaction logic is the same for if a policy is permissive or restrictive with the exception of the values returned when breaking the loop. When finding a value explicitly accepted (restrictive policy) true is returned, while the opposite is true for finding a value explicitly rejected in a permissive policy
     * @param requiredPorts Required ports for outgoing traffic
     * @param exitPolicy Exit policy of a node (format: <accept|reject> <ports>)
     * @return true if policy is satisfied, false otherwise
     */
    public boolean satisfiesPolicy(Set<Integer> requiredPorts, String exitPolicy) {
        String[] policySplit = exitPolicy.split(" ");
        assert policySplit.length == 2;
        String[] policyPorts = policySplit[1].split(",");

        boolean acceptOrReject;
        switch (policyPorts[0]) {
            case "accept":
                acceptOrReject = true;
                break;
            case "reject":
                acceptOrReject = false;
                break;
            default:
                throw new IllegalArgumentException("Invalid policy type: " + policyPorts[0]);
        }

        for (String policyPort : policyPorts) {
            String[] portSplit = policyPort.split("-");
            if (portSplit.length == 1 && requiredPorts.contains(Integer.parseInt(portSplit[0])))  // If it is not a range
                return acceptOrReject;
            else if (portSplit.length == 2) { // If it is a range
                for (int port : requiredPorts) {
                    if (port > Integer.parseInt(portSplit[0]) && port < Integer.parseInt(portSplit[1]))
                        return acceptOrReject;
                }
            }
            else
                throw new RuntimeException("Unexpected exception");
        }
        return !acceptOrReject;
    }
}
