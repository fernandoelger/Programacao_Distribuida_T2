package trab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
    private String host;
    private int port;
    private boolean isSuperNode;

    private Node superNode;
    private List<Node> nodes;

    private HashMap<String, List<NodeFile>> nodeFiles;

    public Node(String host, int port, boolean isSuperNode) {
        this.host = host;
        this.port = port;
        this.isSuperNode = isSuperNode;

        if (this.isSuperNode) {
            this.nodes = new ArrayList<>();
            this.nodeFiles = new HashMap<>();
        }
    }

    //SUPERNODE METHODS
    private void registerFiles(String host, List<NodeFile> files){
        this.nodeFiles.put(host, files);
    }



    //NODE METHODS
















    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSuperNode() {
        return isSuperNode;
    }

    public void setSuperNode(boolean superNode) {
        isSuperNode = superNode;
    }

    public Node getSuperNode() {
        return superNode;
    }

    public void setSuperNode(Node superNode) {
        this.superNode = superNode;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public HashMap<String, List<NodeFile>> getNodeFiles() {
        return nodeFiles;
    }

    public void setNodeFiles(HashMap<String, List<NodeFile>> nodeFiles) {
        this.nodeFiles = nodeFiles;
    }
}
