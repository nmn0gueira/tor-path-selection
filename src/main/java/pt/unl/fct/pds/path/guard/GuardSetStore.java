package pt.unl.fct.pds.path.guard;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.RandomCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface GuardSetStore {

    int GUARD_SET_SIZE = 3;

    Node[] getGuardSet();

    static Node[] createGuardSet(List<Node> nodes) {
        Node[] guardSet = new Node[GUARD_SET_SIZE];
        RandomCollection<Node> suitableNodes = new RandomCollection<>();
        for (Node node : nodes) {
            if (!node.getFlags().contains("Guard"))
                continue;
            if (!node.getFlags().contains("Running"))
                continue;
            suitableNodes.add(node.getBandwidth(), node);
        }

        assert suitableNodes.size() >= GUARD_SET_SIZE;

        Set<Node> seen = new HashSet<>();
        int idx = 0;
        while (idx < GUARD_SET_SIZE) {
            Node candidate = suitableNodes.next();
            if (seen.add(candidate)) {
                guardSet[idx++] = candidate;
            }
        }
        return guardSet;
    }
}
