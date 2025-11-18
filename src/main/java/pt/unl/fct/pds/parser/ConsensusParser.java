package pt.unl.fct.pds.parser;


import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.GeoLookup;
import pt.unl.fct.pds.utils.HexUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class ConsensusParser {
    String filename;
    
    public ConsensusParser() {}
    public ConsensusParser(String filename) {this.filename = filename;}

    public String getFilename() {return filename;}
    public void setFilename(String filename) {this.filename = filename;}

    /*
    r <NodeNickname> <Fingerprint> <DescriptorDigest> <PublicationTime> <IP Address> <ORPort> <DIRPort>
    a <IPv6 address>:<port> (this line is optional)
    s <Flags> (1 or more, options listed below)
    v <VersionNumber>
    pr <SupportedProtocols> (1 or mode, not needed for our project)
    w Bandwidth=<Bandwidth>
    p <ExitPolicy>
     */
    public List<Node> parseConsensus(Map<String, Set<String>> nodeFamilies) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        String line;
        while ((line = reader.readLine()) != null && !line.startsWith("r ")) { // Pass lines ahead until we see one starting with 'r '. That is the start of the nodes
        }

        if (line == null) {
            throw new RuntimeException("Not a valid consensus file.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        GeoLookup gl = new GeoLookup();
        List<Node> nodes = new LinkedList<>();

        while (line != null && line.startsWith("r ")) {
            String[] split = line.split(" ");
            String nickname = split[1];
            String fingerprint = HexUtils.toHex(Base64.getDecoder().decode(split[2])).toUpperCase();  // Store in hex string instead of base64
            String descriptorDigest = HexUtils.toHex(Base64.getDecoder().decode(split[3])).toLowerCase();  // Store in hex string instead of base64
            String dateString = split[4];
            String timeString = split[5];
            String ipAddress = split[6];
            int orPort = Integer.parseInt(split[7]);
            int dirPort = Integer.parseInt(split[8]);

            LocalDateTime dateTime = LocalDateTime.parse(dateString + " " + timeString, formatter);

            line = reader.readLine();
            if (line.startsWith("a "))  // skip IPv6 address line if it exists (we do not use it)
                line = reader.readLine();

            Set<String> flags = new HashSet<>(Arrays.asList(line.substring(2).split(" ")));
            line = reader.readLine();
            String version = line.substring(2);
            reader.readLine();  // Skip pr
            line = reader.readLine();
            int bandwidth = Integer.parseInt(line.split(" ")[1].split("=")[1]); // Split bandwidth line, then grab the second part and split on the equal sign to grab the value
            line = reader.readLine();   // p
            String policy = line.substring(2);
            line = reader.readLine();

            String country = gl.locateCountry(ipAddress);

            Set<String> familyEntries = nodeFamilies.get(fingerprint);
            if (familyEntries == null) {
                throw new RuntimeException("Descriptor not found for node with fingerprint " + fingerprint + ".");
            }

            nodes.add(new Node(
                    nickname,
                    fingerprint,
                    descriptorDigest,
                    dateTime,
                    ipAddress,
                    orPort,
                    dirPort,
                    flags,
                    version,
                    bandwidth,
                    country,
                    policy,
                    familyEntries
            ));
        }

        return nodes;
    }
}
