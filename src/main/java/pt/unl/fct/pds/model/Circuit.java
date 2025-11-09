package pt.unl.fct.pds.model;

import java.util.Arrays;

public class Circuit {
    int id;
    Node[] nodes;
    int minBandwidth;
    
    public Circuit() {}
    public Circuit(
                    int id,
                    Node[] nodes,
                    int minBandwidth
                  ) {
        this.id = id;
        this.nodes = Arrays.copyOf(nodes, nodes.length);
        this.minBandwidth = minBandwidth;
    }

    public Circuit(int id, Node[] nodes) {
        this.id = id;
        this.nodes = Arrays.copyOf(nodes, nodes.length);
        int minBandwidth = nodes[0].getBandwidth();
        for (int i = 1; i < nodes.length; i++){
            if  (nodes[i].getBandwidth() < minBandwidth){
                minBandwidth = nodes[i].getBandwidth();
            }
        }
    }

    public int getId() {return id;}
    public Node[] getNodes() {return nodes;}
    public int getMinBandwidth() {return minBandwidth;}

    
    public void setId(int id) {this.id = id;}
    public void setNodes(Node[] nodes) {this.nodes = Arrays.copyOf(nodes, nodes.length);}
    public void setMinBandwidth(int minBandwidth) {this.minBandwidth = minBandwidth;}
}
