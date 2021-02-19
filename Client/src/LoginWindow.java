import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame {
    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");
    JLabel loginLabel = new JLabel("User name ");
    JLabel passwordLabel = new JLabel("password ");

    public LoginWindow(){
        super("Login");
        this.client = new ChatClient("localhost",8818);
        client.connect();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginLabel);
        p.add(loginField);
        p.add(passwordLabel);
        p.add(passwordField);
        p.add(loginButton);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        getContentPane().add(p, BorderLayout.CENTER);

        pack(); //resize window for the components
        setVisible(true);
    }

    private void doLogin()  {
        String login = loginField.getText();
        String password = passwordField.getText();

        try {
            if (client.login(login,password)){
                //bring up the user list
                UserListGUI userListGUI = new UserListGUI(client);
                JFrame frame = new JFrame(login + "'s User List");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400,600);

                frame.getContentPane().add(userListGUI, BorderLayout.CENTER);
                frame.setVisible(true);
            }else{
                //show error message
                JOptionPane.showMessageDialog(this,"Invalid user or password!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LoginWindow loginWin = new LoginWindow();
        loginWin.setVisible(true);
    }
}
