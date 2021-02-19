import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",8818);

        if (!client.connect()) {
            System.out.println("Connect failed.");
        } else {
            System.out.println("Connect successful");
            //client.logoff();
        }

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("Message from: " + fromLogin + " ===> " + msgBody);
            }
        });
    }

    //in functia connect cream socketul care ne permite sa ne conectam la server
    public boolean connect() {
        try {
            this.socket = new Socket(serverName,serverPort);
            System.out.println("Client port: " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn  =new BufferedReader((new InputStreamReader(serverIn)));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //trimite comanda de mesaj serverului
    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    //apelata in LoginWindow, trimitem comanda de login la server si asteptam raspunsul, in cazul in care login-ul a avut succes, deschidem un thread pentru mesaje
    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";  //comanda ce o trimitem la server
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine(); //raspunsul serverului
        System.out.println("Response Line: " + response);

        if ("Successful login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }
//Thread pentru posibilitatea comunicarii intre multiplii clienti si server, deodata.
    public void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }
    //luam date/comenzi de la server, serverOutputul este inputul clientului.
    public void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {  //loop "infinit"
                String[] tokens = StringUtils.split(line);  //luam comenzi de la server (outputul serverului) si facem split la comanda pentru a putea hotara ce actiune sa luam.
                System.out.println(Arrays.toString(tokens));
                if (tokens != null && tokens.length > 0){
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)){
                        String[] tokensMsg = StringUtils.split(line, null,3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for(MessageListener listener : messageListeners){
            listener.onMessage(login, msgBody);
        }
    }

    public void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners){
            listener.offline(login);
        }
    }

    public void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners){
            listener.online(login);
        }
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }
    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }
}
