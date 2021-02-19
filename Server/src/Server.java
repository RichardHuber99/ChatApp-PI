import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;

    private ArrayList<ClientHandler> clientHandlerList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ClientHandler> getClientHandlerList(){
        return clientHandlerList;
    }

    @Override
    public void run() {   //MAIN THREAD
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true){  //acceptam incontinu conexiuni ale clientilor prin accept().
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(this,clientSocket);
                clientHandlerList.add(clientHandler);
                clientHandler.start();   //Thread pentru fiecare conexiune
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    // functie pentru a scoate conexiuni din lista de clienti
    public void removeClient(ClientHandler client) {
        clientHandlerList.remove(client);
    }
}

