// CSD 2013, Pablo Galdámez

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

//
// Simple ChatChannel implementation
//
public class ChatChannel
        extends UnicastRemoteObject
        implements IChatChannel {
    public static final String LEAVE = "LEAVE";
    public static final String JOIN = "JOIN";

    private String name;
    private Hashtable<String, IChatUser> users = new Hashtable<>();

    public ChatChannel(String name) throws RemoteException {
        super(ChatConfiguration.the().getMyPort());
        this.name = name;
    }

    //
    // ISA IChatChannel
    //
    public String getName() throws RemoteException {
        return name;
    }

    //
    // ISA IChatChannel
    //
    public boolean join(IChatUser usr) throws RemoteException {
        String nick = usr.getNick();
        String keyNick = nick.trim().toLowerCase();
        if (users.get(keyNick) != null) return false; // User already in channel
        users.put(keyNick, usr);
        notifyUsers(JOIN, nick);
        return true;
    }

    //
    // ISA IChatChannel
    //
    public boolean leave(IChatUser usr) throws RemoteException {
        String nick = usr.getNick();
        String keyNick = nick.trim().toLowerCase();
        if (users.get(keyNick) == null) return false; // User not found
        users.remove(keyNick);

        // Channel sends a control message to all users at the channel---> one user left
        notifyUsers(LEAVE, nick);
        return true;
    }

    //
    // ISA IChatChannel
    //
    public void sendMessage(IChatMessage msg) throws RemoteException {
        purge();
        for (IChatUser usr : users.values()) {
            usr.sendMessage(msg);
        }
    }

    //
    // ISA IChatChannel
    //
    public IChatUser[] listUsers() throws RemoteException {
        purge();
        return users.values().toArray(new IChatUser[users.size()]);
    }

    //
    // private function to purge stale users
    //
    private void purge() {
        String[] keys = users.keySet().toArray(new String[users.size()]);
        for (String key : keys) {
            try {
                users.get(key).getNick(); // Remote invokable to check if stale or alive
            } catch (Exception e) {
                users.remove(key); // Remove stale users
                notifyUsers(LEAVE, key);   // this notification sends lowercase nicks (keys)
            }
        }
    }

    //
    // When users leave or join this channel, we send a message to the remaining users, so that
    // they can update their fancy UI's :)
    //
    private void notifyUsers(String code, String nick) {
        IChatMessage msg;
        try {
            msg = new ChatMessage(null, this, code + " " + nick);
        } catch (Exception e) {
            return;
        }

        for (IChatUser usr : users.values()) {
            try {
                usr.sendMessage(msg);
            } catch (Exception e) {
            } // Ignore errors when sending channel notifications
        }

    }

}
