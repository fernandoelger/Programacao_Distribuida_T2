package trab;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
    private String host;
    private int port;
    private boolean isSuperNode;

    private String superNodeHost;
    private int superNodePort;

    //private List<Node> nodes;
    private HashMap<String, List<NodeFile>> superNodeFiles;

    private List<NodeFile> localNodeFiles;

    private DatagramSocket connectionSocket;

    public Node(String host, int port, boolean isSuperNode) {
        this.host = host;
        this.port = port;
        this.isSuperNode = isSuperNode;

        if (this.isSuperNode) {
            //this.nodes = new ArrayList<>();
            this.superNodeFiles = new HashMap<>();

        } else {
            this.localNodeFiles = new ArrayList<>();
        }
    }

    public void registerNode(Node node, List<NodeFile> files) throws SocketException {
        if (!node.isSuperNode()) {
            localNodeFiles = files;
        }

        this.host = node.host;
        this.port = node.port;
        this.isSuperNode = node.isSuperNode;

        connectionSocket = new DatagramSocket(port);
    }


    public void connectToSuper(String host, int port) throws SocketException {
        this.superNodeHost = host;
        this.superNodePort = port;
        //testar se conectou direito

		byte[] saida = new byte[1024];
		saida = ().getBytes();
		DatagramPacket pacote = new DatagramPacket(saida, saida.length, grupo, 5000);
		socket.send(pacote);
    }



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

    // public List<Node> getNodes() {
    //     return nodes;
    // }

    // public void setNodes(List<Node> nodes) {
    //     this.nodes = nodes;
    // }

    public HashMap<String, List<NodeFile>> getSuperNodeFiles() {
        return superNodeFiles;
    }

    public void setSuperNodeFiles(HashMap<String, List<NodeFile>> superNodeFiles) {
        this.superNodeFiles = superNodeFiles;
    }

    public List<NodeFile> getLocalNodeFiles() {
        return localNodeFiles;
    }

    public void setLocalNodeFiles(List<NodeFile> localNodeFiles) {
        this.localNodeFiles = localNodeFiles;
    }

    public DatagramSocket getConnectionSocket() {
        return connectionSocket;
    }

    public void setConnectionSocket(DatagramSocket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }
}
