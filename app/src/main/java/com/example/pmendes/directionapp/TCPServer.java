package com.example.pmendes.directionapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer implements Subscriber{
    private int portAnInt;
    private ServerSocket serverSocket;
    private ArrayList<Socket> clientSockets;
    private ArrayList<PrintWriter> printWriters;
    private ArrayList<BufferedReader> bufferedReaders;
    private boolean isConnected;

    public TCPServer(int _port){
        portAnInt = _port;
        clientSockets = new ArrayList<>();
        printWriters = new ArrayList<>();
        bufferedReaders = new ArrayList<>();
        CreateSocketServer();
        isConnected = false;
    }

    public boolean IsConnected() {
        return isConnected;
    }

    public void SetupConnection(){
        Socket socket = AcceptConnection();
        assert socket != null;
        SetupReader(socket);
        SetupWriter(socket);
        isConnected = true;
    }

    public void CloseConnection() throws IOException {
        for (Socket socket :
                clientSockets) {
            socket.close();
        }
        isConnected = false;
    }

    private void CreateSocketServer(){
        try{
             serverSocket = new ServerSocket(portAnInt);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket AcceptConnection(){
        try {
            Socket clientSocket = serverSocket.accept();
            clientSockets.add(clientSocket);
            return clientSocket;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void SetupWriter(Socket socket){
        try {
            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);
            printWriters.add(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SetupReader(Socket socket){
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            bufferedReaders.add(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Update(OrientationAnalyzer.DIRECTION direction) {
        for (PrintWriter writer :
                printWriters)
            writer.println(direction);
    }
}
