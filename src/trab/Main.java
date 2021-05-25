package trab;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Uso: java Nodos <host> <port> <is super node>");
            return;
        }

        final List<NodeFile> files = List.of(
                new NodeFile("arquivo.txt", "C:\\caminho", "hash"),
                new NodeFile("outro_arquivo.txt", "C:\\caminho", "hash")
        );

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        boolean isSuperNode = Boolean.parseBoolean(args[2]);

        Scanner sc = new Scanner(System.in);

        byte[] bytesPacote = new byte[4096];
        DatagramPacket packet;


        final String REGISTER = "REGISTER";
        final String GET_HOST_WITH_FILE = "GET_HOST_WITH_FILE";

//        String host = "localhost";
//        int port = 8085;
//        boolean isSuperNode = true;

        Node node = new Node(host, port, isSuperNode);

        node.registerNode(node, files);

        System.out.println("Nodo registrado com sucesso, host: " + host + " port: " + port + " super nodo? " + isSuperNode);

        //conectar com o super nodo
        if (!isSuperNode) {

            //requisitar dados de conexao do super node...
            System.out.println("Informe o host do supernode: ");
            String superHost = sc.next();

            System.out.println("Informe o port do supernode:");
            int superPort = sc.nextInt();

            node.connectToSuper(superHost, superPort);
        }


        if (node.isSuperNode) {
            System.out.println("entrou no loop supernodo");

            while (true) {
                packet = new DatagramPacket(bytesPacote, bytesPacote.length, InetAddress.getByName(node.superNodeHost), node.superNodePort);
                node.connectionSocket.receive(packet);

                String receivedString = new String(packet.getData(), 0, packet.getLength());

                String[] request = receivedString.split(" - ");

                String operation = request[0];
                String parameters = request[1];

                switch (operation) {
                    case REGISTER:
                        //List<NodeFile> fodase = Collections.emptyList();
                        List<NodeFile> fodase = files;

                        //FAZER PARSE DE PARAMETERS E USAR A LISTA DE ARQUIVOS

                        node.saveNodeFiles(packet.getAddress().getHostName(), packet.getPort(), fodase);
                        break;

                    case GET_HOST_WITH_FILE:
                        //parameters contém o nome do arquivo solicitado
                        String response = node.getFileHostByName(parameters);

                        bytesPacote = (response).getBytes();

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

                command = sc.nextInt();

                switch (command) {
                    case 1:
                        System.out.println("Digite o nome do arquivo: ");
                        String fileName = "outro_arquivo.txt";//sc.nextLine();

                        bytesPacote = (GET_HOST_WITH_FILE + " - " + fileName).getBytes();

                        packet = new DatagramPacket(bytesPacote, bytesPacote.length, InetAddress.getByName(node.superNodeHost), node.superNodePort);

                        System.out.println("Enviando request de busca por nome do arquivo...");

                        node.connectionSocket.send(packet);

                        node.connectionSocket.receive(packet);

                        String receivedString = new String(packet.getData(), 0, packet.getLength());

                        String[] request = receivedString.split(" - ");

                        String operation = request[0];
                        String parameters = request[1];

                        if (operation.equals("FILE_FOUND")) {
                            //ip:port
                            String[] hostInfo = parameters.split(":");

                            bytesPacote = ("GET_FILE - " + fileName).getBytes();

                            packet = new DatagramPacket(bytesPacote, bytesPacote.length, InetAddress.getByName(hostInfo[0]), Integer.parseInt(hostInfo[1]));

                            node.connectionSocket.send(packet);

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
}
