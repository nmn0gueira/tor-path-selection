package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class ConsensusParser {
    String filename;
    
    public ConsensusParser() {}
    public ConsensusParser(String filename) {this.filename = filename;}

    public String getFilename() {return filename;}
    public void setFilename(String filename) {this.filename = filename;}

    /*
    r <NodeNickname> <Fingerprint> <Digest> <PublicationTime> <IP Address> <ORPort> <DIRPort>
    a <IPv6 address>:<port> (this line is optional)
    s <Flags> (1 or more, options listed below)
    v <VersionNumber>
    pr <SupportedProtocols> (1 or mode, not needed for our project)
    w Bandwidth=<Bandwidth>
    p <ExitPolicy>
     */
    public List<Node> parseConsensus() throws IOException {
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
            String fingerprint = split[2];
            String dateString = split[4];
            String timeString = split[5];
            String ipAddress = split[6];
            int orPort = Integer.parseInt(split[7]);
            int dirPort = Integer.parseInt(split[8]);

            LocalDateTime dateTime = LocalDateTime.parse(dateString + " " + timeString, formatter);

            line = reader.readLine();
            if (line.startsWith("a "))  // skip IPv6 address line if it exists (we do not use it)
                line = reader.readLine();

            String[] flags = line.substring(2).split(" ");
            line = reader.readLine();
            String version = line.substring(2);
            reader.readLine();  // Skip pr
            line = reader.readLine();
            int bandwidth = Integer.parseInt(line.split(" ")[1].split("=")[1]); // Split bandwidth line, then grab the second part and split on the equal sign to grab the value
            line = reader.readLine();   // p
            String policy =  line.substring(2);
            line = reader.readLine();

            String country = gl.locateCountry(ipAddress);
            nodes.add(new Node(
                    nickname,
                    fingerprint,
                    dateTime,
                    ipAddress,
                    orPort,
                    dirPort,
                    flags,
                    version,
                    bandwidth,
                    country,
                    policy
            ));
        }

        return nodes;
    }
}
