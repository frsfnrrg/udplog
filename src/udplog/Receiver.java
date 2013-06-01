package udplog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class Receiver extends Thread {

    public Receiver(int port, PacketHandler p, boolean fakeItP) {
        super();
        inPort = port;
        try {
            serverSocket = new DatagramSocket(inPort);
        } catch (SocketException e) {
            serverSocket = null;
            System.out.println("Socket @" + String.valueOf(inPort)
                    + " failed to open: " + e.getMessage());
        }
        pHandle = p;

        ignorePackets = false;
        pause = false;
        receiveDatas = new byte[1024];
        nonDebug = !fakeItP;
    }

    private final boolean nonDebug;
    private final int inPort;
    private DatagramSocket serverSocket;
    private final PacketHandler pHandle;
    private Boolean ignorePackets = false;
    private boolean pause;
    private final byte[] receiveDatas;

    @Override
    public void run() {
        try {
            System.out.printf("Listening on udp:%s:%d%n", InetAddress
                    .getLocalHost().getHostAddress(), inPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("This computer's IP not identified...");
        }
        while (!isInterrupted()) {
            if (!pause) {
                receiveStuff();
            }
        }
    }

    // External interactions
    public void close() {
        interrupt();
        // so far it burns on close ...
        if (serverSocket != null) {
            serverSocket.close();
            System.out.println("Closed socket.");
        }
    }

    // ReceiveStuff and setEnabled cannot be made "synchronized";
    // receive stuff would hog things the entire time, even during
    // the thread-sleep. To avoid writing and closing files simultaneously,
    // we pass the burden to the PacketHandler.
    // Then again, almost all Java objects have a synchronized on them
    // anyway
    private void receiveStuff() {
        if (nonDebug) {
            if (serverSocket == null) {
                return;
            }

            java.util.Arrays.fill(receiveDatas, (byte) 0);

            DatagramPacket receivePacket = new DatagramPacket(receiveDatas,
                    receiveDatas.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                System.out.println("Failed to receive a packet: "
                        + e.getMessage());
                return;
            }

            // Java sucks at null terminators
            byte[] returnedData = receivePacket.getData();
            int i;
            for (i = 0; i < returnedData.length && returnedData[i] != 0; i++) {
            }
            String sentence = new String(returnedData, 0, i);

            if (!ignorePackets) {
                pHandle.receive(sentence);
            }
        } else {
            if (!ignorePackets) {
                pHandle.receive("D:" + Double.toString(Util.nextSine(1.0))
                        + "," + Double.toString(Util.nextSine(2.2)) + "\n");
                pHandle.receive("E:0," + Double.toString(Util.nextSine(2.0))
                        + "\n");
                if (Util.nextSine(.4) > 0) {
                    pHandle.receive("1,"
                            + Double.toString(Util.nextSine(5.0)
                                    * Util.nextSine(3.21)) + "\n");
                } else {
                    pHandle.receive(Double.toString(Util.nextSine(4.0)
                            * Util.nextSine(4.0))
                            + "\n");
                }
            }

            try {
                Thread.sleep(50);
                // we cut the thread (see close())
            } catch (InterruptedException e) {
            }
        }
    }

    public void setEnabled(Boolean on) {
        ignorePackets = !on;
        if (!on) {
            pHandle.cut();
        }
    }

    public boolean setNewPort(int port) {
        pause = true;
        System.out.format("Changing port to UDP %d.\n", port);

        if (serverSocket != null) {
            serverSocket.close();
        }

        try {
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            serverSocket = null;
            System.out.println("Socket @" + String.valueOf(port)
                    + " failed to open: " + e.getMessage());
            return false;
        } finally {
            pause = false;
        }

        return true;

    }

    public boolean isConnected() {
        return (serverSocket != null);
    }
}