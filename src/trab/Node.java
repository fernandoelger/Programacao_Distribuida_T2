package trab;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.MulticastSocket;

public class Node extends Thread {
    public String host;
    public int port;
    public boolean isSuperNode;

    public String superNodeHost;
    public int superNodePort;

    public HashMap<String, List<NodeFile>> superNodeFiles;

    public List<NodeFile> localNodeFiles;

    public DatagramSocket connectionSocket;

    public DatagramSocket peerSocket;

    public MulticastSocket connectionMulticastSocket;

    public String multicastGroup = "224.0.2.1";

    public int multicastPort = 22355;

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

    @Override
    public void run() {
        if (!isSuperNode) {
            while (true) {
                try {
                    // obtem a resposta
                    byte[] bytesPacote = new byte[4096];

                    DatagramPacket pacote = new DatagramPacket(bytesPacote, bytesPacote.length);
                    peerSocket.setSoTimeout(500);
                    peerSocket.receive(pacote);

                    String resposta = new String(pacote.getData(), 0, pacote.getLength());

                    String[] request = resposta.split(" -- ");

                    String operation = request[1];
                    String parameters = request[2];

                    switch (operation) {
                        case "GET_FILE":
                            NodeFile file = this.localNodeFiles
                                    .stream()
                                    .filter(nodeFile -> nodeFile.getName().equals(parameters))
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("Algum erro de comunicação aconteceu no caminho, nodo recebeu request de arquivo mas não tem esse arquivo"));

                            long timestamp = System.currentTimeMillis();

                            bytesPacote = (Long.toString(timestamp) + " -- RETURN_FILE_HASH -- " + file.getHash()).getBytes();

                            pacote = new DatagramPacket(bytesPacote, bytesPacote.length, pacote.getAddress(), pacote.getPort());

                            peerSocket.send(pacote);

                            break;

                        case "RETURN_FILE_HASH":
                            System.out.println("Arquivo recebido com sucesso!! hash dele é:" + parameters);
                            break;
                    }
                } catch (IOException ex) {
                    //System.out.print(".");
                }
            }
        } else {
            long last_time = 0;
            long actual_time;

            while (true) {
                try {
                    // obtem a resposta do multicast
                    byte[] bytesPacote = new byte[1024];

                    DatagramPacket multicastPacket = new DatagramPacket(bytesPacote, bytesPacote.length);
                    connectionMulticastSocket.setSoTimeout(500);
                    connectionMulticastSocket.receive(multicastPacket);

                    String receivedString = new String(multicastPacket.getData(), 0, multicastPacket.getLength());
                    String[] request = receivedString.split(" -- ");

                    actual_time = Long.parseLong(request[0]);

                    if (actual_time == last_time) {
                        System.out.println("old: " + receivedString);
                    } else {
                        System.out.println("new: " + receivedString);
                        last_time = actual_time;

                        // envia nova mensagem para o multicast
                        byte[] saida = new byte[1024];
                        saida = receivedString.getBytes();
                        InetAddress group = InetAddress.getByName(multicastGroup);
                        multicastPacket = new DatagramPacket(saida, saida.length, group, multicastPort);
                        connectionMulticastSocket.send(multicastPacket);
                    }
                } catch (IOException ex) {
                    //System.out.print(".");
                }
            }
        }
    }

    public void registerNode(Node node) throws IOException {
        if (!node.isSuperNode) {

            int peerPort = node.port + 1;

            System.out.println("Adicionando peer socket na porta " + peerPort);

            peerSocket = new DatagramSocket(peerPort);
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

        long timestamp = System.currentTimeMillis();
        byte[] saida = new byte[4096];

        String stringList = getListString();

        saida = (Long.toString(timestamp) + " -- REGISTER -- " + stringList).getBytes();

        DatagramPacket packet = new DatagramPacket(saida, saida.length, InetAddress.getByName(host), port);
        connectionSocket.send(packet);
    }

    public String getFileHostByName(String fileName) {
        for (Map.Entry<String, List<NodeFile>> entry : superNodeFiles.entrySet()) {
            for (NodeFile file : entry.getValue()) {
                if (file.getName().equals(fileName)) {
                    //achou o arquivo, retorna a connection string do nodo que tem o arquivo solicitado
                    return "FILE_FOUND -- " + entry.getKey();
                }
            }
        }

        return "FILE_NOT_FOUND -- NOT_FOUND";
    }

    private String getListString() {
        //StringBuilder builder = new StringBuilder();
        String teste = "";

        for (NodeFile file : this.localNodeFiles) {
            teste = teste.concat(file.getName() + "---" + file.getPath() + "---" + file.getHash() + "|");

            //            builder.append(file.getName())
//                    .append("---")
//                    .append(file.getPath())
//                    .append("---")
//                    .append(file.getHash())
//                    .append("|");
        }

        return teste;
    }

}
