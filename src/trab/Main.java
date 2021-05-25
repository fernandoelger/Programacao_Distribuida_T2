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
                new NodeFile("arquivo.txt", "C:\\caminho", "hash")
        );

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        boolean isSuperNode = Boolean.parseBoolean(args[2]);

        Scanner sc = new Scanner(System.in);

        byte[] bytesPacote = new byte[4096];
        DatagramPacket packet;

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

                String[] request = receivedString.split("-");

                String operation = request[0];
                String parameters = request[1];

                switch (operation) {
                    case "REGISTER":
                        List<NodeFile> fodase = Collections.emptyList();

                        //FAZER PARSE DE PARAMETERS E USAR A LISTA DE ARQUIVOS

                        node.saveNodeFiles(packet.getAddress().getHostName(), packet.getPort(), fodase);
                        break;

                    case "GET_FILE_BY_NAME":
                        node.getFileByName(parameters);
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

                switch(command){
                    case 1:
                        bytesPacote = ("GET_FILE_BY_NAME").getBytes();

                        packet = new DatagramPacket(bytesPacote, bytesPacote.length, InetAddress.getByName(node.superNodeHost), node.superNodePort);
                        node.connectionSocket.send(packet);

                        //receber pacote resposta

                        break;
                }

            }
        }


    }
}
