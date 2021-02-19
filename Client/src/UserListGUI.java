import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class UserListGUI extends JPanel implements UserStatusListener {

    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;  //for presence <==> UserStatusListener

    public UserListGUI(ChatClient client){
        this.client = client;
        this.client.addUserStatusListener(this);   //adaugam clientul in lista userilor online

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);  //lista userilor online
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        //click pe nume user --> se deschide fereastra de mesaje
        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1){
                    String login = userListUI.getSelectedValue();
                    MessageGUI messageGUI = new MessageGUI(client, login);

                    JFrame f = new JFrame("Message: " + login);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setSize(500,500);
                    f.getContentPane().add(messageGUI,BorderLayout.CENTER);
                    f.setVisible(true);
                }
            }
        });
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }
    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}
