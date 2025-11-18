package pt.unl.fct.pds;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.ConsensusParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 
{
    private static final String CACHED_CONSENSUS = "cache/consensus";
    private static final String CACHED_SV_DESCRIPTORS = "cache/sv_descriptors";
    private static final String DEFAULT_TRAFFIC = "example/traffic";

    public static void main(String[] args) throws IOException {
        // Very simple arg parser
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argMap.put(args[i], args[i + 1]);
        }
        String consensusFile = argMap.get("--consensus");
        if (consensusFile == null) {
            consensusFile = CACHED_CONSENSUS;
            if (!(new File(CACHED_CONSENSUS).exists()))
                download(
                        "http://171.25.193.9:443/tor/status-vote/current/consensus",
                        Paths.get(CACHED_CONSENSUS),
                        true,
                        bytes -> {
                            System.out.printf("Downloading consensus (%f MB)...\r", bytes * 1.0/(1024 * 1024));
                        });
        }
        String serverDescriptorsFile = argMap.get("--server-descriptors");
        if (serverDescriptorsFile == null) {
            serverDescriptorsFile = CACHED_SV_DESCRIPTORS;
            if (!(new File(CACHED_SV_DESCRIPTORS).exists()))
                download(
                        "http://171.25.193.9:443/tor/server/all",
                        Paths.get(CACHED_SV_DESCRIPTORS),
                        true,
                        bytes -> {
                            System.out.printf("Downloading server descriptors (%f MB)...\r", bytes * 1.0/(1024 * 1024));
                        });
        }

        String trafficFile = argMap.get("--traffic");

        System.out.println("Welcome to the Circuit Simulator!");

        ConsensusParser parser = new ConsensusParser(consensusFile);
        //List<Node> nodes = parser.parseConsensus();

        //PathSelection torPathSelection = new TorPathSelection(nodes);
        //Circuit circuit = torPathSelection.buildCircuit();
    }

    /**
     * Downloads a file from the given URL to the specified destination path.
     *
     * @param fileURL  the URL of the file to download
     * @param target   the local path where the file should be saved
     * @throws IOException if an I/O error occurs
     */
    public static void download(String fileURL, Path target, boolean createDirectories, Consumer<Long> progressCallback) throws IOException {
        if (createDirectories && target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(30_000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned HTTP " + responseCode
                    + " for " + fileURL);
        }

        Path temp = Files.createTempFile(target.getParent(), "download", ".tmp");
        try (InputStream in = connection.getInputStream();
             OutputStream out = Files.newOutputStream(temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            byte[] buffer = new byte[8192];
            long total = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                total += read;
                if (progressCallback != null) {
                    progressCallback.accept(total);
                }

            }
        } finally {
            connection.disconnect();
        }
        Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
