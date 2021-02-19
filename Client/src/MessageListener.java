/**
 * User1 mesaj pt User2
 *  mesaj (user1) --> server ---> mesaj (ajunge la user2)
 */
public interface MessageListener {
    public void onMessage(String fromLogin, String msgBody);
}
