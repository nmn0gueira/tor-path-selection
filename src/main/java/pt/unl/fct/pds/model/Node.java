package pt.unl.fct.pds.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

public class Node implements Serializable {
    private String nickname;
    private String fingerprint;
    private String descriptorDigest;
    private LocalDateTime timePublished;
    private String ipAddress;
    private int orPort;
    private int dirPort;
    private Set<String> flags;
    private String version;
    private int bandwidth;
    private String country;
    private String exitPolicy;
    private Set<String> family;
    private String subnet;

    public Node() {}

    // IMPORTANT: Sets are passed by reference and not copied.
    public Node(
                String nickname,
                String fingerprint,
                String descriptorDigest,
                LocalDateTime timePublished,
                String ipAddress,
                int orPort,
                int dirPort,
                Set<String> flags,
                String version,
                int bandwidth,
                String country,
                String exitPolicy,
                Set<String> family)
    {
        this.nickname = nickname;
        this.fingerprint = fingerprint;
        this.descriptorDigest = descriptorDigest;
        this.timePublished = timePublished;
        this.ipAddress = ipAddress;
        this.orPort = orPort;
        this.dirPort = dirPort;
        this.flags = flags;
        this.version = version;
        this.bandwidth = bandwidth;
        this.country = country;
        this.exitPolicy = exitPolicy;
        this.family = family;
        String[] ipSplit = ipAddress.split("\\.");
        assert ipSplit.length == 4;
        this.subnet = String.join(".", ipSplit[0], ipSplit[1]);
    }

    public String getNickname() {return nickname;}
    public String getFingerprint() {return fingerprint;}
    public String getDescriptorDigest() {return descriptorDigest;}
    public LocalDateTime getTimePublished() {return timePublished;}
    public String getIpAddress() {return ipAddress;}
    public int getOrPort() {return orPort;}
    public int getDirPort() {return dirPort;}
    public Set<String> getFlags() {return flags;}
    public String getVersion() {return version;}
    public int getBandwidth() {return bandwidth;}
    public String getCountry() {return country;}
    public String getExitPolicy() {return exitPolicy;}
    public Set<String> getFamily() {return family;}
    public String getSubnet() {return subnet;}

    
    public void setNickname(String nickname) {this.nickname = nickname;}
    public void setFingerprint(String fingerprint) {this.fingerprint = fingerprint;}
    public void setDescriptorDigest(String descriptorDigest) {this.descriptorDigest = descriptorDigest;}
    public void setTimePublished(LocalDateTime timePublished) {this.timePublished =timePublished;}
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        String[] ipSplit = ipAddress.split("\\.");
        assert ipSplit.length == 4;
        this.subnet = String.join(".", ipSplit[0], ipSplit[1]);
    }
    public void setOrPort(int orPort) {this.orPort = orPort;}
    public void setDirPort(int dirPort) {this.dirPort = dirPort;}
    public void setFlags(Set<String> flags) { this.flags = flags;}
    public void setVersion(String version) {this.version = version;}
    public void setBandwidth(int bandwidth) {this.bandwidth = bandwidth;}
    public void setCountry(String country) {this.country = country;}
    public void setExitPolicy(String exitPolicy) {this.exitPolicy = exitPolicy;}
    public void setFamily(Set<String> family) {this.family = family;}


    public boolean isInSame16Subnet(Node otherNode) {
        return this.subnet.equals(otherNode.getSubnet());
    }

    /**
     * From <a href="https://spec.torproject.org/path-spec/path-selection-constraints.html?highlight=family#family-membership">...</a> (...) two relays belong to the same family if each relay lists the other relay in its family list.
     * @param otherNode
     * @return
     */
    public boolean isInSameFamily(Node otherNode) {
        // Both nodes need to list each other in their family entries (either by fingerprint or nickname)
        return this.family.contains(otherNode.getFingerprint()) || this.family.contains(otherNode.getNickname())
                && (otherNode.getFamily().contains(this.fingerprint) || otherNode.getFamily().contains(this.nickname));
    }

    /**
     * This snippet below may seem confusing. The policy satisfaction logic is the same for if a policy is permissive or restrictive except for the values returned when breaking the loop. When finding a value explicitly accepted (restrictive policy) true is returned, while the opposite is true for finding a value explicitly rejected in a permissive policy
     * @param destinationPort Required port for outgoing traffic
     * @return true if policy is satisfied, false otherwise
     */
    public boolean satisfiesPolicy(int destinationPort) {
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

    @Override
    public String toString() {
        return "Node{" +
                "nickname='" + nickname + '\'' +
                ", fingerprint='" + fingerprint + '\'' +
                ", descriptorDigest='" + descriptorDigest + '\'' +
                ", timePublished=" + timePublished +
                ", ipAddress='" + ipAddress + '\'' +
                ", orPort=" + orPort +
                ", dirPort=" + dirPort +
                ", flags=" + flags +
                ", version='" + version + '\'' +
                ", bandwidth=" + bandwidth +
                ", country='" + country + '\'' +
                ", exitPolicy='" + exitPolicy + '\'' +
                ", family=" + family +
                '}';
    }
}
