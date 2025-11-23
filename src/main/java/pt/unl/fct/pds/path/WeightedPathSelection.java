package pt.unl.fct.pds.path;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.NetworkUtils;
import pt.unl.fct.pds.utils.RandomCollection;
import pt.unl.fct.pds.utils.Cache;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class WeightedPathSelection extends AbstractPathSelection {

    private static final double ALPHA = Math.random();
    private static final double BETA = Math.random();

    public WeightedPathSelection(List<Node> nodes) {
        super(nodes);
        id = 2;
    }

    @Override
    public Node getGuardNode(Node exitNode) {

        boolean hasGuardSet = false;
        for (Node g : guardSet) if (g != null) { hasGuardSet = true; break; }

        RandomCollection<Node> rc = new RandomCollection<>();
        boolean added = false;

        if (hasGuardSet) {
            for (Node node : guardSet) {
                if (node == null) continue;
                if (!node.getFlags().contains("Guard")) continue;

                boolean mutualFamily = (node.getFamily().contains(exitNode.getFingerprint())
                        || node.getFamily().contains(exitNode.getNickname()))
                        && (exitNode.getFamily().contains(node.getFingerprint())
                                || exitNode.getFamily().contains(node.getNickname()));
                if (mutualFamily) continue;

                if (NetworkUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress())) continue;

                double w = node.getBandwidth();
                if (!node.getCountry().equals(exitNode.getCountry())) w *= (1 + ALPHA);

                rc.add(w, node);
                added = true;
            }
        } else {
            RandomCollection<Node> selector = new RandomCollection<>();
            for (Node node : nodes) {
                if (node == null) continue;
                if (!node.getFlags().contains("Guard")) continue;
                if (!node.getFlags().contains("Running")) continue;
                selector.add(node.getBandwidth(), node);
            }

            Node[] newGuardSet = new Node[3];
            for (int i = 0; i < newGuardSet.length; i++) newGuardSet[i] = selector.next();

            Path cachePath = Cache.getCachePath("guard_set");
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(cachePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE))) {
                out.writeObject(newGuardSet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (Node node : newGuardSet) {
                if (node == null) continue;

                boolean mutualFamily = (node.getFamily().contains(exitNode.getFingerprint())
                        || node.getFamily().contains(exitNode.getNickname()))
                        && (exitNode.getFamily().contains(node.getFingerprint())
                                || exitNode.getFamily().contains(node.getNickname()));
                if (mutualFamily) continue;

                if (NetworkUtils.same16Subnet(node.getIpAddress(), exitNode.getIpAddress())) continue;

                double w = node.getBandwidth();
                if (!node.getCountry().equals(exitNode.getCountry())) w *= (1 + ALPHA);

                rc.add(w, node);
                added = true;
            }
        }

        if (!added) return null;
        return rc.next();
    }

    @Override
    public Node getMiddleNode(Node guardNode, Node exitNode) {
        RandomCollection<Node> rc = new RandomCollection<>();
        return rc.next();
    }

}
