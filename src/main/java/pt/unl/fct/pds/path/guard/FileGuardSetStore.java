package pt.unl.fct.pds.path.guard;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.Cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileGuardSetStore implements GuardSetStore {

    private static final Path DEFAULT_CACHE_PATH = Cache.getCachePath("guard_set");
    private final Node[] guardSet;

    public FileGuardSetStore() {
        this.guardSet = loadGuardSet(DEFAULT_CACHE_PATH);
    }

    public FileGuardSetStore(Path cachePath) {
        this.guardSet = loadGuardSet(cachePath);
    }

    public FileGuardSetStore(List<Node> nodes, Path cachePath) {
        this.guardSet = loadOrCreateGuardSet(nodes, cachePath);
    }

    public FileGuardSetStore(List<Node> nodes) {
        this.guardSet = loadOrCreateGuardSet(nodes, DEFAULT_CACHE_PATH);
    }

    private Node[] loadOrCreateGuardSet(List<Node> nodes, Path cachePath) {
        if (Files.exists(cachePath)) {
            return loadGuardSet(cachePath);
        }
        Node[] newSet = GuardSetStore.createGuardSet(nodes);
        writeGuardSet(newSet, cachePath);
        return newSet;
    }

    private Node[] loadGuardSet(Path cachePath) {
        try (ObjectInputStream oi = new ObjectInputStream(Files.newInputStream(cachePath, StandardOpenOption.READ))) {
            Object object = oi.readObject();
            return (Node[]) object; // We do not verify if the cached guard set is valid in this simpler behavior
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeGuardSet(Node[] guardSet, Path cachePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(cachePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE))) {
            out.writeObject(guardSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Node[] getGuardSet() {
        return guardSet;
    }
}
