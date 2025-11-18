package pt.unl.fct.pds.parser;
import org.torproject.descriptor.*;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class ServerDescriptorParser {
    String filename;
    private static final Logger logger = Logger.getLogger(ServerDescriptorParser.class.getName());

    public ServerDescriptorParser() {}
    public ServerDescriptorParser(String filename) {this.filename = filename;}

    public String getFilename() {return filename;}
    public void setFilename(String filename) {this.filename = filename;}

    public Map<String, ServerDescriptor> parseServerDescriptors() {
        DescriptorReader descriptorReader = DescriptorSourceFactory.createDescriptorReader();
        Map<String, ServerDescriptor> serverDescriptors = new HashMap<>();
        Set<String> fingerprints = new HashSet<>();

        long startCheckpoint = System.currentTimeMillis();
        logger.info("Parsing server descriptors...");
        for (Descriptor descriptor : descriptorReader.readDescriptors(new File(filename))) {
            if (!(descriptor instanceof ServerDescriptor)) {
                continue;
                //throw new RuntimeException("Descriptor in cache is not a ServerDescriptor");
            }
            ServerDescriptor serverDescriptor = (ServerDescriptor) descriptor;
            serverDescriptors.put(serverDescriptor.getFingerprint(), serverDescriptor);
            if (!fingerprints.add(serverDescriptor.getFingerprint())) {
                System.out.println("Duplicate fingerprint: " + serverDescriptor.getFingerprint());
            }
        }
        logger.info("Done. Took " + (System.currentTimeMillis() - startCheckpoint) + " milliseconds");
        return serverDescriptors;
    }
}



