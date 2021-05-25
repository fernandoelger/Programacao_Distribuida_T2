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

    public String multicastGroup = "localhost";

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

                    String operation = request[0];
                    String parameters = request[1];

                    switch (operation) {
                        case "GET_FILE":
                            NodeFile file = this.localNodeFiles
                                    .stream()
                                    .filter(nodeFile -> nodeFile.getName().equals(parameters))
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("Algum erro de comunicação aconteceu no caminho, nodo recebeu request de arquivo mas não tem esse arquivo"));

                            bytesPacote = ("RETURN_FILE_HASH -- " + file.getHash()).getBytes();

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
        }
    }

    public void registerNode(Node node) throws IOException {
        if (!node.isSuperNode) {

            int peerPort = node.port + 1;

            System.out.println("Adicionando peer socket na porta " + peerPort);

            peerSocket = new DatagramSocket(peerPort);
        } else {
            connectionMulticastSocket = new MulticastSocket(multicastPort);
//            InetAddress grupo = InetAddress.getByName(multicastGroup); // ip do grupo Multicast
//		    connectionMulticastSocket.joinGroup(grupo);
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
        byte[] saida = new byte[4096];
        //saida = (Long.toString(timestamp) + " - REGISTER - lista de arquivos").getBytes();

        String stringList = getListString();
        saida = ("REGISTER -- " + stringList).getBytes();

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
