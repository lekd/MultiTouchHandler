package com.example.lkduy.multitouchhandler;

import java.net.ServerSocket;
import java.net.Socket;

public class StreamingServer implements Runnable {
    ServerSocket serverSocket;
    private int serverPort = 4040;

    public void setEventListener(StreamingServerEventListener eventListener) {
        this.eventListener = eventListener;
    }

    StreamingServerEventListener eventListener;
    public StreamingServer(int serverPort)
    {
        this.serverPort = serverPort;
    }
    @Override
    public void run() {

        try
        {
            serverSocket = new ServerSocket(serverPort);
            while(!Thread.currentThread().isInterrupted())
            {
                try
                {
                    Socket sock = serverSocket.accept();
                    if(eventListener != null)
                    {
                        eventListener.ClientConnectedEvent(sock);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            serverSocket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
    public  interface StreamingServerEventListener
    {
        void ClientConnectedEvent(Socket clientSock);
    }
}
