package trab;

import java.net.DatagramSocket;
import java.net.SocketException;
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

        Node node = new Node(host, port, isSuperNode);

        node.registerNode(node, files);

        //conectar com o super nodo
        if (!isSuperNode) {
            Scanner sc = new Scanner(System.in);

            //requisitar dados de conexao do super node...
            System.out.println("Informe o host do supernode: ");
            String superHost = sc.next();

            System.out.println("Informe o port do supernode:");
            int superPort = sc.nextInt();

            node.connectToSuper(superHost, superPort);
        }


        if (node.isSuperNode()) {
            while (true) {

            }
        } else {
            while (true) {

            }
        }


    }
}
