package com.myfirstchat.netChat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

/*
* class represents one connection
* */
public class TCPConnection {

    private final Socket socket;  // A socket is an endpoint for communication between two machines.
    private final Thread rxThread; // thread that listens to input connection
    private final TCPConnectionListener eventListener;  // event listener

    // for working with strings
    private final BufferedReader in;
    private final BufferedWriter out;

    // overloaded constructor
    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    // constractor
    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.socket = socket;
        this.eventListener = eventListener;

        // getting input and output threads from socket to read and to write
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        // thread will listen to TCPConnection
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while(!rxThread.isInterrupted()) {      // while the thread continues
                        eventListener.onReceiveString(TCPConnection.this, in.readLine()); // get input string
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);     // dicsonnect anyway
                }
            }
        });
        rxThread.start();
    }

    /* synchronize methods to be able to use them in different threads */

    // method writes input string to the buffered writer
    public synchronized void sendString(String value) {
        try {
            out.write(value + "\r\n");              // \r - return caret
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    // method interrupts the thread and closes connection
    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override // standard method toString
    public String toString(){
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
