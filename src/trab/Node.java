package trab;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.MulticastSocket;

public class Node {
    public String host;
    public int port;
    public boolean isSuperNode;

    public String superNodeHost;
    public int superNodePort;

    public HashMap<String, List<NodeFile>> superNodeFiles;

    public List<NodeFile> localNodeFiles;

    public DatagramSocket connectionSocket;

    public MulticastSocket connectionMulticastSocket;

    public String multicastGroup = "172.168.0.1";

    public int multicastPort = 5000;

    public Node(String host, int port, boolean isSuperNode) {
        this.host = host;
        this.port = port;
        this.isSuperNode = isSuperNode;

        if (this.isSuperNode) {
            this.superNodeFiles = new HashMap<>();

        } else {
            this.localNodeFiles = new ArrayList<>();
        }
    }

    public void registerNode(Node node, List<NodeFile> files) throws SocketException, IOException {
        if (!node.isSuperNode) {
            localNodeFiles = files;
        } else {
            connectionMulticastSocket = new MulticastSocket(multicastPort);
            InetAddress grupo = InetAddress.getByName(multicastGroup); // ip do grupo Multicast
		    connectionMulticastSocket.joinGroup(grupo);
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

        long timestamp = System.currentTimeMillis();
        byte[] saida;
        saida = (Long.toString(timestamp) + " - REGISTER - lista de arquivos").getBytes();
        DatagramPacket packet = new DatagramPacket(saida, saida.length, InetAddress.getByName(host), port);
        connectionSocket.send(packet);
    }

    public String getFileHostByName(String fileName) {
        for (Map.Entry<String, List<NodeFile>> entry : superNodeFiles.entrySet()) {
            for (NodeFile file : entry.getValue()) {
                if(file.getName().equals(fileName)){
                    //achou o arquivo, retorna a connection string do nodo que tem o arquivo solicitado
                    return "FILE_FOUND - " + entry.getKey();
                }
            }
        }

        return "FILE_NOT_FOUND - NOT_FOUND";
    }

}
