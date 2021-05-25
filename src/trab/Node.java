package trab;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
    public String host;
    public int port;
    public boolean isSuperNode;

    public String superNodeHost;
    public int superNodePort;

    public HashMap<String, List<NodeFile>> superNodeFiles;

    public List<NodeFile> localNodeFiles;

    public DatagramSocket connectionSocket;

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
        if (!node.isSuperNode) {
            localNodeFiles = files;
        }

        this.host = node.host;
        this.port = node.port;
        this.isSuperNode = node.isSuperNode;

        connectionSocket = new DatagramSocket(port);
        connectionSocket.setSoTimeout(5000000);
    }

    public void saveNodeFiles(String host, int port, List<NodeFile> files) {
        superNodeFiles.put(host + ":" + port, files);
    }


    public void connectToSuper(String host, int port) throws IOException {
        this.superNodeHost = host;
        this.superNodePort = port;
        //testar se conectou direito

        byte[] saida;
        saida = ("REGISTER - lista de arquivos").getBytes();
        DatagramPacket packet = new DatagramPacket(saida, saida.length, InetAddress.getByName(host), port);
        connectionSocket.send(packet);
    }

    public String getFileByName(String fileName) {
        return null;
    }

}
