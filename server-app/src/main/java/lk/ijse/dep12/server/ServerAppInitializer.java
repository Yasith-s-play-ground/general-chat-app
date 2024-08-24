package lk.ijse.dep12.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerAppInitializer {
    private static final List<Socket> CLIENT_LIST = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(5050)) {
            while (true) {
                Socket localSocket = serverSocket.accept();
                CLIENT_LIST.add(localSocket);
                broadCastMessage(localSocket, "Entered: " + localSocket.getRemoteSocketAddress() + "\n");

                new Thread(() -> {
                    try {
                        InputStream is = localSocket.getInputStream();
                        byte[] byteBuffer = new byte[1024];
                        while (true) {
                            int read = is.read(byteBuffer);
                            if (read == -1) {
                                broadCastMessage(localSocket, "Exited: " + localSocket.getRemoteSocketAddress() + "\n");
                                CLIENT_LIST.remove(localSocket); // remove this socket after it exited
                                break;
                            }
                            String message = new String(byteBuffer, 0, read);
                            broadCastMessage(localSocket, message);
                            Arrays.fill(byteBuffer, (byte) 0);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }

    }

    private static void broadCastMessage(Socket client, String message) {
        new Thread(() -> {

            for (Socket socket : CLIENT_LIST) {
                if (socket == client) continue;
                try {
                    if (socket.isConnected()) {
                        socket.getOutputStream().write(message.getBytes());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            //same can be done like this
//            CLIENT_LIST.stream().filter(socket -> socket != client).forEach(socket -> {
//                try {
//                    socket.getOutputStream().write(message.getBytes());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });

        }).start();
    }
}
