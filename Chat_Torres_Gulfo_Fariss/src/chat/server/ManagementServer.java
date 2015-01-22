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
import chat.descriptors.messages.UserAfterFinishGroupMsg;
import implementation.engine.NioEngineImpl;
import implementation.engine.NioServerImpl;
import implementation.engine.AcceptCallback;
import implementation.engine.ConnectCallback;
import implementation.engine.DeliverCallback;
import implementation.engine.NioChannelImpl;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.TreeSet;
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
    private static final String GROUP_NAME = "000GROUP000000000001"; // For the future!
    private int userIndexStack = 0;

    private TreeSet<ReceivedDeliverMsg> deliverList;
    private BitSet activeUsers;

    //Threads
    private Thread nioLoop;

    //Reinsertion Users
    private LinkedList<RemoteUser> newUserAfterFinish; //

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
        this.deliverList = new TreeSet<ReceivedDeliverMsg>();
        this.activeUsers = new BitSet();
        this.newUserAfterFinish = new LinkedList<RemoteUser>();
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
                //e.printStackTrace();
                System.err.println("Starting server in port " + i + " not possible.");
            }
        }
        if (server != null && server.getPort() > 0) {
            System.out.println("Running server on port " + server.getPort() + "...");
        } else {
            throw new ServerException("Server can not start in ports " + minPort + " - "
                    + maxPort + ".");
        }
    }

    public void initServer() {
        this.nioLoop = new Thread(this.nioEngine, "NioLoopServer");
        this.nioLoop.start(); //TODO: Watch thread behavior
        try {
            startListening();
        } catch (ServerException ex) {
            System.err.println("Server can not be initialize.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void accepted(NioServerImpl server, NioChannelImpl channel) {
        if (this.userIndexStack < this.maxUsers) { //If the group is not complete
            channel.setDeliverCallback(this); // Set callback of incomming message
            RemoteUser user = new RemoteUser(channel, userIndexStack++);
            try {
                this.group.add(user);
                this.nioEngine.registerNioChannel(user, SelectionKey.OP_READ);
            } catch (ClosedChannelException ex) {
                //TODO;
                channel.close();
                System.err.println("New channel can not be registred. The connection has been closed.");
            }
        } else { // If the group is complete
            //server.close(); // Stop accepting new connections
            channel.setDeliverCallback(this); // Set callback of incomming message
            RemoteUser user = new RemoteUser(channel, userIndexStack++);
            try {
                this.newUserAfterFinish.add(user);
                this.nioEngine.registerNioChannel(user, SelectionKey.OP_READ);
            } catch (ClosedChannelException ex) {
                //TODO;
                channel.close();
                System.err.println("New channel can not be registred. The connection has been closed.");
            }
        }
    }

    @Override
    public void closed(NioChannelImpl channel) {
        //TODO: User reconnection
        //When tis method is called, the key for this channel has been 
        //erased. The only things to do is to lock fro the user in the lis of members
        //and then add it to the possible reconnection list
        RemoteUser user = whoHasThisNioChannel(channel);
        this.activeUsers.clear(user.getIndex());
        this.group.remove(user);//Erase user from the list
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
                //PAYLOAD: listeningPort (int)
                if (newUserAfterFinish.isEmpty()) {
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
                } else {
                    //new user after group finish
                    int portValeu = bytes.getInt();
                    RemoteUser user = (RemoteUser) getUserAfterFinish(channel);
                    if (user.getPort() < 0) { // Listening port not setted 
                        user.setPort(portValeu);
                        System.out.println("User " + user.toString() + ": Listening port setted in " + user.getPort());
                        this.group.add(user);
                        this.newUserAfterFinish.remove(user);
                        updateNewIncomingUser(user);
                    } else { // print incomming message
                        //TODO: Usefull to debug
                        System.out.println("User " + user.toString() + " sends: \n" + portValeu);
                    }
                }
                break;

            case Message.ACK_SERVER:
                System.out.println("--Server Ack--");
                break;

            case Message.DELIVER_MSG:
                //TODO: Compare msg
                long lc = bytes.getLong();
                RemoteUser u = whoHasThisNioChannel(channel);
                ReceivedDeliverMsg msg = getReceivedDeliverMsgByLc(lc);
                if (msg != null) {
                    msg.setDeliver(u.getIndex());
                    verifyDeliver();
                } else {
                    this.deliverList.add(
                            new ReceivedDeliverMsg(lc, activeUsers, u.getIndex()));
                    verifyDeliver();
                }
                //System.out.println("Client" + u.getIndex() + " deliver Message LC= " + lc);
                break;

            case Message.USER_LEAVE_MSG:
                //TODO:
                //
                int index = bytes.getInt();
                System.err.println("Client" + index + " leave chat room.");
                break;

            case Message.COMINGBACK_MSG:
                //TODO:
                int i = bytes.getInt();
                System.err.println("Client" + i + " leave chat room.");
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

    public RemoteUser getUserAfterFinish(NioChannelImpl nioChannel) {
        Iterator it;
        it = newUserAfterFinish.iterator();
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
                this.activeUsers.set(user.getIndex());
                multicastMessage(new GroupMemberMsg(user.getIndex(), user.getIp(), user.getPort()));
            }
        }

    }

    public void updateNewIncomingUser(RemoteUser user) {
        user.addMessageToQueue(new GroupeFinishMsg(GROUP_NAME, group.size(), user.getIndex()));
        Iterator it = this.group.iterator();
        while (it.hasNext()) {
            RemoteUser u = (RemoteUser) it.next();
            user.addMessageToQueue(new GroupMemberMsg(u.getIndex(), u.getIp(), u.getPort()));
        }
        
        this.nioEngine.changeOpInterest(user.getNioChannel(),
                SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        
        multicastMessage(new UserAfterFinishGroupMsg(user.getIndex(), user.getIp(), user.getPort()) );
        this.activeUsers.set(user.getIndex());
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

    public void verifyDeliver() {
        try {
            if (deliverList.first().allDeliverComplete()) {
                System.out.println(deliverList.pollFirst().toString()
                        + " was well delived by all users");
                verifyDeliver();
            } else if (deliverList.first().timeOutComplete()) {
                System.err.println(deliverList.pollFirst().toString()
                        + " time out. [Not delivered]");
                //verifyDeliver();
            }
        } catch (NoSuchElementException e) {
            //No element in the list
        }
    }

    /**
     * Look for incoming ack in the list with the same lc
     *
     * @param lc
     * @return null is there is not
     */
    public ReceivedDeliverMsg getReceivedDeliverMsgByLc(long lc) {
        Iterator<ReceivedDeliverMsg> it = deliverList.iterator();
        while (it.hasNext()) {
            ReceivedDeliverMsg msg = it.next();
            if (msg.getLcMessage() == lc) {
                return msg;
            }
        }
        return null;
    }

}
