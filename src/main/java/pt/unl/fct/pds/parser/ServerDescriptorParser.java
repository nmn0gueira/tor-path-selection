package pt.unl.fct.pds.parser;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class ServerDescriptorParser {
    String filename;
    private static final Logger logger = Logger.getLogger(ServerDescriptorParser.class.getName());

    public ServerDescriptorParser() {}
    public ServerDescriptorParser(String filename) {this.filename = filename;}

    public String getFilename() {return filename;}
    public void setFilename(String filename) {this.filename = filename;}

    /*
    We only care about the fingerprint and family
     */
    public Map<String, Set<String>> parseServerDescriptors() throws IOException {
        long startCheckpoint = System.currentTimeMillis();
        logger.info("Parsing server descriptors...");

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        Map<String, Set<String>> familyByFingerprint = new HashMap<>();
        String line = reader.readLine();
        String fingerprint = null;

        while (line != null) {
            if (line.startsWith("fingerprint ")) {
                String[] fingerprintParts = line.split(" ");
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < fingerprintParts.length; i++) {
                    sb.append(fingerprintParts[i]);
                }
                fingerprint = sb.toString();
                familyByFingerprint.put(fingerprint, new HashSet<>());
            }
            if (line.startsWith("family ")) {
                Set<String> familySet = getFamilySet(line);
                familyByFingerprint.put(fingerprint, familySet);
            }
            line = reader.readLine();
        }
        logger.info("Done. Took " + (System.currentTimeMillis() - startCheckpoint) + " milliseconds");
        return familyByFingerprint;
    }

    /*
    Family entries may be of the form one of the following forms:
    - nickname
    - $fingerprint
    - $fingerprint=nickname
    - $fingerprint~nickname
    We identify by fingerprint preferably and then by nickname. No family includes both an entry for a nickname and a fingerprint for the same node
     */
    private static Set<String> getFamilySet(String line) {
        String[] familyParts = line.split(" ");
        Set<String> lastFamily = new HashSet<>(familyParts.length - 1);
        for (int i = 1; i < familyParts.length; i++) {
            if (familyParts[i].startsWith("$")) {
                String[] memberSplit = familyParts[i].split("[=~]"); // Split on = or ~

                if (memberSplit.length == 1) { // Fingerprint only
                    lastFamily.add(familyParts[i].substring(1));
                }
                else if (memberSplit.length == 2) { // Fingerprint and nickname tuple
                    lastFamily.add(memberSplit[0].substring(1)); // We only store fingerprint anyway here
                }
            }
            else {  // Nickname only
                lastFamily.add(familyParts[i].substring(1));
            }
        }
        return lastFamily;
    }
}



