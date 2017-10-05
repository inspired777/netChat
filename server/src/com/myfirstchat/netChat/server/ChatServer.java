package com.myfirstchat.netChat.server;

import com.myfirstchat.netChat.network.TCPConnection;
import com.myfirstchat.netChat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * server can:  receive input connections,
 *              keep a number of input connections open,
 *              send messages

 *       class ServerSocket: can listen input connection,
 *                               receive input connection,
 *                               create the object of Socket,
 *                               give away a socket that is ready.
 *
 *       class Socket can set connection.
 * */
public class ChatServer implements TCPConnectionListener{

    private final ArrayList<TCPConnection> connections = new ArrayList<>();     // all made collections

    // first - run the server, then - run the client
    public static void main(String[] args) {
        new ChatServer();
    }

    /* constructor */
    private ChatServer() {

        System.out.println("Server is running...");

        try (ServerSocket serverSocket = new ServerSocket(8189)) {          // creating new socket
            while (true) {                                                        // endless cycle
                try {
                    new TCPConnection(this, serverSocket.accept());     // creating new TCP connection
                } catch (IOException ex) {
                    System.out.println("TCPCOnnection exception: " + ex);
                }
            }
        }
        catch (IOException e ) {
            throw new RuntimeException(e);
        }
    }

    /* implementing TCPConnectionListener interface */
    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        /*  add current connection to list and tell all listeners about it.  */
        connections.add(tcpConnection);
        sendToAllConnections("Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        /* send given message to all listeners */
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        /* remove current connection from list and tell all listeners about it */
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }

    /* notify all listeners with string */
    private void sendToAllConnections(String value) {
        System.out.println(value);

        for(TCPConnection con : connections) {
            con.sendString(value);
        }
    }
}
