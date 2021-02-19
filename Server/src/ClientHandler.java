import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ClientHandler extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    /**
     * Thread pentru a putea trata fiecare conexiune in parte, in acelasi timp.
     */
    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**   Info comenzi:
     *     login <user> <passwd>                                                   #topic <---- chatroom / group chat
     *     logoff || quit                                                           join #topic
     *     msg <user> text...     (ex. "msg guest Hello World")                     leave #topic
     *                                                                              msg #topic text...
     */
    private void handleClient() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");   // split la comanda/mesaj, delimitand prin spatiu
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];                 //primul cuvant din comanda/mesaj il salvam in cmd, apoi in functie de cmd vedem ce actiune facem
                if ("quit".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                }else if ("login".equalsIgnoreCase(cmd)){
                    handleLogin(outputStream,tokens);
                }else if("msg".equalsIgnoreCase(cmd)){
                    String[] tokensMsg = StringUtils.split(line,null,3);
                    handleMessage(tokensMsg);
                } else if("join".equalsIgnoreCase(cmd)){  // nu e in UI, doar in terminal. join #topic ,  msg #topic text
                    handleJoin(tokens);
                } else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }


    public boolean isMemberOfTopic(String topic){
        return topicSet.contains(topic);
    }
    // ex: join #groupChat
    private void handleJoin(String[] tokens) {
        if (tokens.length > 1){
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    //format: "msg" "login" text...
    //format: "msg" "#topic" body...
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ClientHandler> clientHandlerList = server.getClientHandlerList();
        for (ClientHandler client : clientHandlerList){
            if (isTopic){
                if (client.isMemberOfTopic(sendTo)){
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    client.send(outMsg);
                }
            }else{
                if (sendTo.equalsIgnoreCase(client.getLogin())) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    client.send(outMsg);
                }
            }
        }
    }

    private void handleLogoff() throws IOException {
        server.removeClient(this);
        List<ClientHandler> clientHandlerList = server.getClientHandlerList();
        //send other online users current user's status
        String onlineBroadcastMsg = "offline " + login + "\n";
        for(ClientHandler client : clientHandlerList){
            if (!login.equals(client.getLogin()))
                client.send(onlineBroadcastMsg);
        }
        clientSocket.close();
    }

    public String getLogin(){
        return login;
    }


    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];

            if(login.equals("guest") && password.equals("guest") || login.equals("Richi") && password.equals("password") || login.equals("anon") && password.equals("anon")){
                String msg = "Successful login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully: " + login);

                List<ClientHandler> clientHandlerList = server.getClientHandlerList(); //lista clienti online

                // clientul primeste info de online de la restul clientilor care sunt online
                for(ClientHandler client : clientHandlerList){
                    if (client.getLogin() != null) {
                        if (!login.equals(client.getLogin())) {
                            String msg2 = "online " + client.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }
                // clientul trimtie statut online la restul clientilor
                String onlineBroadcastMsg = "online " + login + "\n";
                for(ClientHandler client : clientHandlerList){
                    if (!login.equals(client.getLogin())) {
                        client.send(onlineBroadcastMsg);
                    }
                }
            }else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.out.println("Login failed for " + login);
            }
        }
    }

    private void send(String onlineBroadcastMsg) throws IOException {
        if (login != null){
            try{
                outputStream.write(onlineBroadcastMsg.getBytes());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
