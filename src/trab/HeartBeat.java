package trab;

import java.io.IOException;
import java.net.*;

public final class HeartBeat extends Thread {

    private final String nodeHost;
    private final int nodePort;

    private final String superNodeHost;
    private final int superNodePort;

    private final DatagramSocket connectionSocket;

    public HeartBeat(String nodeHost, int nodePort, DatagramSocket connectionSocket, String superNodeHost, int superNodePort) throws SocketException {
        this.nodeHost = nodeHost;
        this.nodePort = nodePort;


        this.connectionSocket = connectionSocket;

        this.superNodeHost = superNodeHost;
        this.superNodePort = superNodePort;
    }

    @Override
    public void run() {
        DatagramPacket packet;

        String message = "IGNORE_THIS -- HEARTBEAT -- " + nodeHost + ":" + nodePort;

        byte[] saida = message.getBytes();

        while (true){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                packet = new DatagramPacket(saida, saida.length, InetAddress.getByName(superNodeHost), superNodePort);

                connectionSocket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
