package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.Cache;
import pt.unl.fct.pds.utils.RandomCollection;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public abstract class AbstractPathSelection implements PathSelection {

    protected int id;
    protected final List<Node> nodes;
    protected final Node[] guardSet;
    private static final Path CACHED_GUARD_SET = Cache.getCachePath("guard_set");

    protected AbstractPathSelection(List<Node> nodes) {
        this.nodes = nodes;
        this.guardSet = getGuardSet();
    }

    protected Node getExitNode(int destinationPort) {
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for  (Node node : nodes) {
            if (!node.getFlags().contains("Exit"))
                continue;
            if (!node.getFlags().contains("Fast"))
                continue;
            if (!satisfiesPolicy(destinationPort, node.getExitPolicy()))
                continue;
            suitableNodes.add(node.getBandwidth(), node);
        }
        return suitableNodes.next();
    }

    protected abstract Node getGuardNode(Node exitNode);

    protected abstract Node getMiddleNode(Node guardNode, Node exitNode);

    private Node[] getGuardSet() {
        if (Files.exists(CACHED_GUARD_SET)) {
            try (ObjectInputStream oi = new ObjectInputStream(Files.newInputStream(CACHED_GUARD_SET, StandardOpenOption.READ))) {
                Object object = oi.readObject();
                return (Node[]) object; // TODO: Maybe further check if cached guard set is valid no? Might not be necessary for the project ig
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            Node[] guardSet = new Node[3];
            RandomCollection<Node> suitableNodes = new RandomCollection<>();
            for (Node node : nodes) {
                if (!node.getFlags().contains("Guard"))
                    continue;
                if (!node.getFlags().contains("Running"))
                    continue;
                suitableNodes.add(node.getBandwidth(), node);
            }
            for (int i = 0; i < guardSet.length; i++) {
                guardSet[i] = suitableNodes.next(); // TODO: Should probably guarantee that the nodes we get here are all different (should be unlikely to happen tho)
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(CACHED_GUARD_SET,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE))) {
                out.writeObject(guardSet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return guardSet;
        }
    }

    @Override
    public Circuit buildCircuit(int destinationPort) {
        Node exitNode = getExitNode(destinationPort);
        Node guardNode = getGuardNode(exitNode);
        Node middleNode = getMiddleNode(guardNode, exitNode);
        Node[] circuitNodes = new Node[]{guardNode, middleNode, exitNode};
        return new Circuit(id, circuitNodes);
    }

    /**
     * This snippet below may seem confusing. The policy satisfaction logic is the same for if a policy is permissive or restrictive except for the values returned when breaking the loop. When finding a value explicitly accepted (restrictive policy) true is returned, while the opposite is true for finding a value explicitly rejected in a permissive policy
     * @param destinationPort Required port for outgoing traffic
     * @param exitPolicy Exit policy of a node (format: <accept|reject> <ports>)
     * @return true if policy is satisfied, false otherwise
     */
    public boolean satisfiesPolicy(int destinationPort, String exitPolicy) {
        String[] policySplit = exitPolicy.split(" ");
        assert policySplit.length == 2;
        String[] policyPorts = policySplit[1].split(",");

        boolean acceptOrReject;
        switch (policySplit[0]) {
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
            if (portSplit.length == 1) { // If it is not a range
                if (destinationPort == Integer.parseInt(portSplit[0]))
                    return acceptOrReject;
            }
            else if (portSplit.length == 2) { // If it is a range
                if (destinationPort >= Integer.parseInt(portSplit[0]) && destinationPort <= Integer.parseInt(portSplit[1]))
                    return acceptOrReject;
            }
            else {
                throw new RuntimeException("Unexpected exception");
            }

        }
        return !acceptOrReject;
    }
}
