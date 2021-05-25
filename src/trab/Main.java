package trab;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) throws Exception {
//        if (args.length != 3) {
//            System.out.println("Uso: java Nodos <host> <port> <is super node>");
//            return;
//        }
//
//        String host = args[0];
//        int port = Integer.parseInt(args[1]);
//        boolean isSuperNode = Boolean.parseBoolean(args[2]);

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        byte[] bytesPacote = new byte[4096];
        DatagramPacket packet;

        final String REGISTER = "REGISTER";
        final String GET_HOST_WITH_FILE = "GET_HOST_WITH_FILE";

        String host = "localhost";
        int port = 8081;
        boolean isSuperNode = true;

        Node node = new Node(host, port, isSuperNode);

        node.registerNode(node);

        System.out.println("Nodo registrado com sucesso, host: " + host + " port: " + port + " super nodo? " + isSuperNode);

        //conectar com o super nodo
        if (!isSuperNode) {

            System.out.println("Informe o caminho da pasta com arquivos: ");
            String folderPath = System.getProperty("user.dir") + "/files/" + input.readLine();

            File folder = new File(folderPath);
            File[] files = folder.listFiles();

            node.localNodeFiles = getFiles(files);

            //requisitar dados de conexao do super node...
            System.out.println("Informe o host do supernode: ");
            String superHost = "localhost";//sc.next();

            System.out.println("Informe o port do supernode:");
            int superPort = 8085;
            //int superPort = Integer.parseInt(input.readLine());

            node.connectToSuper(superHost, superPort);
        }

        node.start();

        if (node.isSuperNode) {
            System.out.println("entrou no loop supernodo");

            while (true) {
                packet = new DatagramPacket(bytesPacote, bytesPacote.length, InetAddress.getByName(node.superNodeHost), node.superNodePort);
                node.connectionSocket.receive(packet);

                String receivedString = new String(packet.getData(), 0, packet.getLength());

                String[] request = receivedString.split(" -- ");

                // request[0] é o timestamp
                String operation = request[1];
                String parameters = request[2];

                switch (operation) {
                    case REGISTER:

                        // envia nova mensagem para o multicast
                        byte[] saida = new byte[1024];
                        saida = receivedString.getBytes();
                        InetAddress group = InetAddress.getByName(node.multicastGroup);
                        DatagramPacket multicastPacket = new DatagramPacket(saida, saida.length, group, node.multicastPort);
                        node.connectionMulticastSocket.send(multicastPacket);

                        //FAZER PARSE DE PARAMETERS E USAR A LISTA DE ARQUIVOS
                        List<NodeFile> files = parseList(parameters);

                        node.saveNodeFiles(packet.getAddress().getHostName(), packet.getPort(), files);

                        System.out.println("node registrado com sucesso");
                        break;

                    case GET_HOST_WITH_FILE:
                        //parameters contém o nome do arquivo solicitado
                        String response = node.getFileHostByName(parameters);

                        long timestamp = System.currentTimeMillis();

                        bytesPacote = (Long.toString(timestamp) + " -- " + response).getBytes();
                        //bytesPacote = response.getBytes();

                        packet = new DatagramPacket(bytesPacote, bytesPacote.length, packet.getAddress(), packet.getPort());

                        node.connectionSocket.send(packet);
                        break;
                }
            }
        } else {
            System.out.println("entrou no loop nodo comum");

            int command;

            while (true) {
                System.out.println("Qual operação deseja fazer?");
                System.out.println("1 - Buscar arquivo por nome");
                //todo.........

                command = Integer.parseInt(input.readLine());

                switch (command) {
                    case 1:
                        System.out.println("Digite o nome do arquivo: ");
                        String fileName = input.readLine();

                        long timestamp = System.currentTimeMillis();
                        bytesPacote = (Long.toString(timestamp) + " -- " + GET_HOST_WITH_FILE + " -- " + fileName).getBytes();

                        packet = new DatagramPacket(bytesPacote, bytesPacote.length, InetAddress.getByName(node.superNodeHost), node.superNodePort);

                        System.out.println("Enviando request de busca por nome do arquivo...");

                        node.connectionSocket.send(packet);

                        node.connectionSocket.receive(packet);

                        String receivedString = new String(packet.getData(), 0, packet.getLength());

                        String[] request = receivedString.split(" -- ");


                        String operation = request[1];
                        String parameters = request[2];

                        if (operation.equals("FILE_FOUND")) {
                            //ip:port
                            String[] hostInfo = parameters.split(":");

                            timestamp = System.currentTimeMillis();

                            bytesPacote = (Long.toString(timestamp) + " -- " + "GET_FILE -- " + fileName).getBytes();

                            packet = new DatagramPacket(bytesPacote, bytesPacote.length, InetAddress.getByName(hostInfo[0]), Integer.parseInt(hostInfo[1]) + 1);

                            node.peerSocket.send(packet);

                            //P2P?

                        } else if (operation.equals("FILE_NOT_FOUND")) {
                            System.out.println("Arquivo não encontrado! tente outro nome de arquivo");
                            continue;
                        } else {
                            throw new RuntimeException("Não sei que comando é esse não");
                        }

                        break;
                }

            }
        }


    }

    private static List<NodeFile> parseList(String listString) {
        String[] objects = listString.split("\\|");

        List<NodeFile> files = new ArrayList<>();

        for (int i = 0; i < objects.length; i++) {
            String[] split = objects[i].split("---");

            files.add(new NodeFile(split[0], split[1], split[2]));
        }

        return files;
    }

    public static List<NodeFile> getFiles(File[] filesArray){
        List<NodeFile> files = new ArrayList<>();

        for (File file : filesArray) {
            files.add(new NodeFile(file.getName(), file.getPath(),

                    //deve ser o hash do arquivo
                    String.valueOf(ThreadLocalRandom.current().nextInt(10000, 100000))
            ));
        }

        return files;
    }
}
