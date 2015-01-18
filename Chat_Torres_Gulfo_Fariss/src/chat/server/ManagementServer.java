/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server;

import chat.descriptors.RemoteHost;
import chat.descriptors.RemoteUser;
import chat.descriptors.messages.GroupMemberMsg;
import chat.descriptors.messages.GroupeFinishMsg;
import chat.descriptors.messages.Message;
import implementation.engine.NioEngineImpl;
import implementation.engine.NioServerImpl;
import implementation.engine.AcceptCallback;
import implementation.engine.ConnectCallback;
import implementation.engine.DeliverCallback;
import implementation.engine.NioChannelImpl;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aquilest
 */
public class ManagementServer implements AcceptCallback, ConnectCallback, DeliverCallback {

    private NioEngineImpl nioEngine = null;
    private NioServerImpl server = null;
    private int minPort;
    private int maxPort;
    private int maxUsers;
    private LinkedList<RemoteUser> group; // Stock users
    private static final String GROUP_NAME = "000GROUP000000000001";

    //Threads
    private Thread nioLoop;

    public ManagementServer(int minPort, int maxPort, int maxUsers) throws ServerException {
        try {
            this.nioEngine = new NioEngineImpl();
        } catch (Exception e) {
            throw new ServerException("Server can not be created");
        }
        this.minPort = minPort;
        this.maxPort = maxPort;
        this.maxUsers = maxUsers;
        this.group = new LinkedList<RemoteUser>();
    }

    public ManagementServer() throws ServerException {
        this(5000, 5959, 3);
    }

    public void startListening() throws ServerException {
        for (int i = minPort; i <= maxPort; i++) {
            try {
                server = this.nioEngine.listen(i, this);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Starting server in port " + i + " not possible.");
            }
        }
        if (server != null && server.getPort() > 0) {
            System.out.println("Running server on port " + server.getPort());
        } else {
            throw new ServerException("Server can not start in ports " + minPort + " - "
                    + maxPort + ".");
        }
    }

    public void initServer() throws ServerException {
        this.nioLoop = new Thread(this.nioEngine, "NioLoopServer");
        this.nioLoop.start(); //TODO: Watch thread behavior
        startListening();
    }

    @Override
    public void accepted(NioServerImpl server, NioChannelImpl channel) {
        if (group.size() < this.maxUsers) { //If the group is not complete
            channel.setDeliverCallback(this); // Set callback of incomming message
            RemoteUser user = new RemoteUser(channel, group.size() + 1);
            try {
                this.group.add(user);
                this.nioEngine.registerNioChannel(user, SelectionKey.OP_READ);
            } catch (ClosedChannelException ex) {
                //TODO;
                channel.close();
                System.err.println("New channel can not be registred. The connection has been closed.");
            }
        } else { // If the group is complete
            server.close(); // Stop accepting new connections
        }
    }

    @Override
    public void closed(NioChannelImpl channel) {
        //TODO: User reconnection
        //When tis method is called, the key for this channel has been 
        //erased. The only things to do is to lock fro the user in the lis of members
        //and then add it to the possible reconnection list
        RemoteUser user = whoHasThisNioChannel(channel);
        channel.close();
        System.out.println("User: " + user.toString() + " is out.");
    }

    @Override
    public void connected(NioChannelImpl channel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deliver(NioChannelImpl channel, ByteBuffer bytes) { //IncomingPayLoad
        // bytes ready to read, no flip needed
        int typeValeu = bytes.getInt();

        switch (typeValeu) {
            case Message.LISTENING_PORT_MSG:
                //PAYLOAD: listenigPort (int)
                int portValeu = bytes.getInt();
                RemoteHost user = (RemoteHost) whoHasThisNioChannel(channel);
                if (user.getPort() < 0) { // Listening port not setted 
                    user.setPort(portValeu);
                    System.out.println("User " + user.toString() + ": Listening port setted in " + user.getPort());
                    groupFinish();
                } else { // print incomming message
                    //TODO: Usefull to debug
                    System.out.println("User " + user.toString() + " sends: \n" + portValeu);
                }
                break;
                
            case Message.ACK_SERVER:
                System.out.println("--Server Ack--");
                break;
                
            case Message.DELIVER_MSG:
                //TODO: Compare msg
                long lc = bytes.getLong();
                RemoteUser u = whoHasThisNioChannel(channel);
                System.out.println("Client"+u.getIndex()+" deliver Message LC= "+ lc);
                break;
        }
    }

    public RemoteUser whoHasThisNioChannel(NioChannelImpl nioChannel) {
        Iterator it;
        it = group.iterator();
        while (it.hasNext()) {
            RemoteUser user = (RemoteUser) it.next();
            if (user.getNioChannel().equals(nioChannel)) {
                return user;
            }
        }
        return null;
    }

    /**
     * send a msg to all member of the group
     *
     * @param msg
     */
    public void multicastMessage(Message msg) {
        Iterator it = this.group.iterator();
        while (it.hasNext()) {
            RemoteUser user = (RemoteUser) it.next();
            user.addMessageToQueue(msg);
            this.nioEngine.changeOpInterest(user.getNioChannel(),
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    public void groupFinish() {
        if (group.size() == this.maxUsers && membersReady()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                Logger.getLogger(ManagementServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            sendGroupFinishMsg();

            Iterator it = this.group.iterator();

            while (it.hasNext()) {
                RemoteUser user = (RemoteUser) it.next();
                multicastMessage(new GroupMemberMsg(user.getIndex(), user.getIp(), user.getPort()));
            }
        }

    }

    public void sendGroupFinishMsg() {
        Iterator it = this.group.iterator();
        while (it.hasNext()) {
            RemoteUser user = (RemoteUser) it.next();
            user.addMessageToQueue(new GroupeFinishMsg(GROUP_NAME, group.size(), user.getIndex()));
            this.nioEngine.changeOpInterest(user.getNioChannel(),
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    public boolean membersReady() {
        Iterator it = this.group.iterator();

        while (it.hasNext()) {
            RemoteHost user = (RemoteHost) it.next();
            if (user.getPort() <= 0) {
                return false;
            }
        }

        return true;
    }

}
