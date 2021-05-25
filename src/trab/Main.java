package trab;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Uso: java Nodos <host> <port> <is super node>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        boolean isSuperNode = Boolean.parseBoolean(args[2]);

        Node node = new Node(host, port, isSuperNode);

        Scanner sc = new Scanner(System.in);

        if (!isSuperNode) {
            //requisitar dados de conexao do super node...
            System.out.println("Informe o host do supernode: ");
            String superHost = sc.next();

            System.out.println("Informe o port do supernode:");
            int superPort = sc.nextInt();

            node.setSuperNode(new Node(superHost, superPort, false));

            DatagramSocket supernode = new DatagramSocket(port);


        }

        if(node.isSuperNode()){

        }


    }
}
