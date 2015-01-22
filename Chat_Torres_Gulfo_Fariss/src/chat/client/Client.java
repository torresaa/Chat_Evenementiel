/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.client;

import chat.descriptors.RemoteServer;
import chat.descriptors.RemoteUser;
import chat.descriptors.messages.AckServer;
import chat.descriptors.messages.ComingBackMsg;
import chat.descriptors.messages.DataAckMsg;
import chat.descriptors.messages.DataMsg;
import chat.descriptors.messages.DeliverMsg;
import chat.descriptors.messages.ListeningPortMsg;
import chat.descriptors.messages.Message;
import chat.descriptors.messages.MyIndexMsg;
import chat.descriptors.messages.UserLeaveMsg;
import chat.gui.ChatException;
import chat.gui.ChatGUI;
import chat.gui.IChatRoom;
import chat.gui.test.EventPump;
import java.nio.ByteBuffer;
import implementation.engine.AcceptCallback;
import implementation.engine.ConnectCallback;
import implementation.engine.DeliverCallback;
import implementation.engine.NioChannelImpl;
import implementation.engine.NioEngineImpl;
import implementation.engine.NioServerImpl;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aquilest
 */
public class Client implements AcceptCallback, ConnectCallback, DeliverCallback, IChatRoom {

    private NioEngineImpl nioEngine;
    private NioServerImpl localServer = null;
    private RemoteServer remoteServer = null;

    private int minPort;
    private int maxPort;
    private LinkedList<RemoteUser> group = null; // Stock users
    private LinkedList<RemoteUser> dieUsers = null; // Stock users
    private BitSet activeUsers;

    private String groupName = null;
    private int numberOfGroupMembers = -1;
    private int myIndex = -1;

    private long lc;
    private TreeSet<ReceivedMsg> msgList;
    private boolean userOut = false;

    //Thread
    private Thread nioLoop;

    //Interface
    IChatListener m_listener;
    String m_name;
    boolean m_inRoom = false;
    EventPump m_pump;
    boolean rushBoolean = false;

    public Client(String name, int minPort, int maxPort) throws ClientException {

        try {
            this.nioEngine = new NioEngineImpl();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ClientException("Client can not be created.");
        }

        this.minPort = minPort;
        this.maxPort = maxPort;
        this.msgList = new TreeSet<ReceivedMsg>();
        this.activeUsers = new BitSet();
        this.m_name = name;
    }

    public void contactServer(String host, int port) throws ClientException {
        try {
            InetAddress ip = InetAddress.getByName(host);
            this.nioEngine.connect(ip, port, this);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Remote host is not reachable.");
            throw new ClientException("Remote host unreacheable");
        }
    }

    public void startListening() throws ClientException {
        for (int i = minPort; i <= maxPort; i++) {
            try {
                localServer = this.nioEngine.listen(i, this);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Starting server in port " + i + " not possible.");
            }
        }
        if (localServer != null && localServer.getPort() > 0) {
            System.out.println("Running server on port " + localServer.getPort());
        } else {
            throw new ClientException("Server can not start in ports " + minPort + " - "
                    + maxPort + ".");
        }
    }

    public void initClient(String host, int port) throws ClientException {
        this.nioLoop = new Thread(this.nioEngine, "NioLoopClient");
        this.nioLoop.start();
        startListening();
        contactServer(host, port);
    }

    @Override
    public void accepted(NioServerImpl server, NioChannelImpl channel) {
        channel.setDeliverCallback(this); // Set callback of incomming message
        RemoteUser user = new RemoteUser(channel, -1);
        try {
            this.nioEngine.registerNioChannel(user, SelectionKey.OP_READ);
        } catch (ClosedChannelException ex) {
            //TODO;
            channel.close();
            System.err.println("New channel can not be registred. The connection has been closed.");
        }
    }

    @Override
    public void closed(NioChannelImpl channel) {
        RemoteUser user = whoHasThisNioChannel(channel);
        this.activeUsers.clear(user.getIndex());
        this.group.remove(user);//Erase user from the list
        m_listener.left("Client" + user.getIndex());
        channel.close();
    }

    @Override
    public void connected(NioChannelImpl channel) {
        if (this.remoteServer == null) {
            try {
                // Server not defined, that means this is the first connected
                channel.getChannel().finishConnect();
                channel.setDeliverCallback(this);
                this.remoteServer = new RemoteServer(channel);
                this.remoteServer.addMessageToQueue(new ListeningPortMsg(this.localServer.getPort()));
                this.nioEngine.registerNioChannel(remoteServer, SelectionKey.OP_WRITE);
                System.out.println("Server registed.");
            } catch (Exception ex) {
                ex.printStackTrace();
                //TODO: Exception because channel cant be registred;
            }
        } else {
            try {
                //
                channel.getChannel().finishConnect();
                channel.setDeliverCallback(this);
                RemoteUser user = whoHasThisNioChannel(channel);

                m_listener.joined("Client" + user.getIndex());

                try {
                    Thread.sleep(1000); // Delay for local test (Synchro)
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }

                user.addMessageToQueue(new MyIndexMsg(this.myIndex));
                this.nioEngine.registerNioChannel(user, SelectionKey.OP_WRITE);
                this.activeUsers.set(user.getIndex());// Set active user
                System.out.println("User registred");
            } catch (Exception ex) {
                ex.printStackTrace();
                //TODO: Exception because channel cant be registred;
            }
        }
    }

    public RemoteUser whoHasThisNioChannel(NioChannelImpl nioChannel) {
        Iterator it;
        it = group.iterator();
        while (it.hasNext()) {
            RemoteUser user = (RemoteUser) it.next();
            if (user.getNioChannel() != null) {
                if (user.getNioChannel().equals(nioChannel)) {
                    return user;
                }
            }
        }
        return null;
    }

    @Override
    public void deliver(NioChannelImpl channel, ByteBuffer bytes) {
        // bytes ready to read, no flip needed
        int typeValeu = bytes.getInt();

        switch (typeValeu) {
            case Message.GROUP_FINISH_MSG:
                byte[] nameBytes = new byte[20];
                bytes.get(nameBytes);
                 {
                    try {
                        this.groupName = new String(nameBytes, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                this.group = new LinkedList<RemoteUser>();
                this.numberOfGroupMembers = bytes.getInt();
                this.myIndex = bytes.getInt();
                //Init LC with index
                this.lc = this.myIndex;
                System.out.println("New group created, name: " + this.groupName
                        + " number of members: " + this.numberOfGroupMembers + ""
                        + " and my index: " + this.myIndex);
                //Start User interface
                new ChatGUI("Client" + this.myIndex, this);
                break;
            case Message.GROUP_MEMBER_MSG:
                int indexValeu = bytes.getInt();
                byte[] ipBytes = new byte[4];
                bytes.get(ipBytes);
                InetAddress ip = null;
                 {
                    try {
                        ip = InetAddress.getByAddress(ipBytes);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                int listenigPort = bytes.getInt();
                RemoteUser user = new RemoteUser(ip, listenigPort, indexValeu);
                //if (indexValeu != myIndex) {//if it is not me
                user.setConnectedCallback(this);
                this.group.add(user);
                System.out.println("--new member: " + user.toString());
                //}

                if (group.size() == this.numberOfGroupMembers) {
                    this.remoteServer.addMessageToQueue(new AckServer());
                    nioEngine.changeOpInterest(channel,
                            SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    initChat();
                }
                break;
            case Message.MY_INDEX_MSG:
                //PAYLOAD = remoteIndex (int)
                int remoteIndexValeu = bytes.getInt();
                RemoteUser u = getUserByIndex(remoteIndexValeu);
                u.setNioChannel(channel);
                nioEngine.changeKeyAttach(channel, getUserByIndex(remoteIndexValeu));

                m_listener.joined("Client" + u.getIndex());

                System.out.println("NioChannel for user: " + u.toString() + " set.");
                this.activeUsers.set(remoteIndexValeu); //New member seted
                break;
            case Message.DATA_MSG:
                long lc = bytes.getLong();
                byte[] data = new byte[bytes.limit() - bytes.position()];
                bytes.get(data);
                incomingDataMsg(lc, data);

                break;

            case Message.DATA_ACK_MSG:
                long lc2 = bytes.getLong();
                incomingAck(whoHasThisNioChannel(channel).getIndex(), lc2);

                break;

            case Message.USER_LEAVE_MSG:
                //TODO:
                int index = bytes.getInt();
                System.err.println("Client" + index + " leave chat room.");
                break;

            case Message.USER_AFTER_FINISH_GROUP:
                int indexValeu2 = bytes.getInt();
                if (indexValeu2 != this.myIndex) {
                    byte[] ipBytes2 = new byte[4];
                    bytes.get(ipBytes2);
                    InetAddress ip2 = null;
                    {
                        try {
                            ip2 = InetAddress.getByAddress(ipBytes2);
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    int listenigPort2 = bytes.getInt();
                    RemoteUser user2 = new RemoteUser(ip2, listenigPort2, indexValeu2);
                    user2.setConnectedCallback(this);
                    this.group.add(user2);
                    System.out.println("--new member: " + user2.toString());

                    contactUser(user2);
                }
                break;
        }

    }

    /**
     * Call by interface in order to send a message format the input string to
     * MsgData
     *
     * @param msg
     */
    public void sendDataMessage(String msg) {
        DataMsg formatMsg = new DataMsg(this.lc, msg);
        multicastMessage(formatMsg);
        this.lc++;//Add this event to LC
        //--Add it to my list of received
        incomingDataMsg(formatMsg.getLcMessage(), formatMsg.getData().getBytes());
        //--Ack for this msg
        incomingAck(myIndex, formatMsg.getLcMessage());
    }

    /**
     * Add the message to list of recived message in order to wait all acks to
     * be deliver.
     *
     * @param lc
     * @param data
     */
    public void incomingDataMsg(long lc, byte[] data) {
        ReceivedMsg msg = new ReceivedMsg(lc, this.activeUsers, data);
        this.msgList.add(msg);
        this.lc = Math.max(this.lc, lc) + 1;
        sendAckMessage(lc);
        incomingAck(myIndex, lc);
    }

    public void sendAckMessage(long lc) {
        multicastMessage(new DataAckMsg(lc));
        this.lc++;
    }

    /**
     * Refresh the list of ack received for the message LC then call
     * verifyDeliver
     *
     * @param userIndex
     * @param lc
     */
    public void incomingAck(int userIndex, long lc) {
        ReceivedMsg msg = getReceivedMsgByLc(lc);
        if (msg != null) {
            msg.setIncomingAck(userIndex);
            verifyDeliver();
        } else {
            System.out.println("Msg " + lc + "is not in the list, Ack rejected.");
        }
    }

    /**
     * verify if he first msg in the queue can be deliver
     *
     */
    public void verifyDeliver() {
        //TODO: suport for disable users
        try {
            if (msgList.first().allAcksComplete()) {
                ReceivedMsg msg = msgList.pollFirst();
                this.remoteServer.addMessageToQueue(new DeliverMsg(msg.getLcMessage()));
                nioEngine.changeOpInterest(this.remoteServer.getNioChannel(),
                        SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                displayMessage(msg.getData());
                verifyDeliver();//Recursive
            } else {
                if ((System.currentTimeMillis() - msgList.first().getTimeReception())
                        > ReceivedMsg.WAIT_TIME_MILIS) {
                    //if the max wait time was complete
                    ReceivedMsg msg = msgList.pollFirst();
                    String err_msg = "ERROR: Message " + msg.getLcMessage() + " deliver time out.";
                    this.displayMessage(err_msg.getBytes());
                }
            }
        } catch (NoSuchElementException ex) {
            //Noting to do, no element in the list
        }
    }

    /**
     * When leaving chat room, called by GUI call back leave.
     */
    public void leavingChatRoom() {
        brodcastMessage(new UserLeaveMsg(myIndex));
        this.userOut = true;
    }

    /**
     * Re-enter after being out, message sended to server (only with leaving)
     */
    public void comingbackToChatRoom() {
        if (this.userOut) {
            remoteServer.addMessageToQueue(new ComingBackMsg(myIndex));
            nioEngine.changeOpInterest(remoteServer.getNioChannel(),
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            m_inRoom = true;
            userOut = false;
        }
    }

    /**
     * print data in the user's interface
     *
     * @param data
     */
    public void displayMessage(byte[] data) {
        String msg;
        try {
            msg = new String(data, "UTF-8");
            m_listener.deliver(msg);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * return the msg in the msgList that has this LC
     *
     * @param lc
     * @return received msg with lc
     */
    public ReceivedMsg getReceivedMsgByLc(long lc) {
        Iterator<ReceivedMsg> it = msgList.iterator();
        while (it.hasNext()) {
            ReceivedMsg msg = it.next();
            if (msg.getLcMessage() == lc) {
                return msg;
            }
        }
        return null;
    }

    /**
     * Send message to all members in the group list
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

    /**
     * Send msg to server and all users
     *
     * @param msg
     */
    public void brodcastMessage(Message msg) {
        this.remoteServer.addMessageToQueue(msg);
        nioEngine.changeOpInterest(remoteServer.getNioChannel(),
                SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        multicastMessage(msg);
    }

    public RemoteUser getUserByIndex(int index) {
        Iterator it = group.iterator();
        while (it.hasNext()) {
            RemoteUser user = (RemoteUser) it.next();
            if (user.getIndex() == index) {
                return user;
            }
        }
        return null;
    }

    /**
     * init connnection with the member of the group that have bigger index than
     * this clien;
     */
    public void initChat() {
        System.out.println("Init Chat");
        try {
            Thread.sleep(20); //Wait interface 
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        //m_listener.joined("Client" + this.myIndex);
        m_listener.joined("Client" + this.myIndex);
        Iterator it = this.group.iterator();
        while (it.hasNext()) {
            RemoteUser user = (RemoteUser) it.next();
            if (user.getIndex() == myIndex) {
                it.remove();
            }
            if (user.getIndex() > this.myIndex) {
                contactUser(user);
            }
        }
    }

    public void contactUser(RemoteUser user) {
        try {
            this.nioEngine.connect(user.getIp(), user.getPort(), user);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Remote host is unreachable.");
            //TODO: 
        }
    }

    // IChatRommInterface
    @Override
    public void enter(String clientName, IChatListener l) throws ChatException {
        if (m_inRoom) {
            throw new ChatException("Already in the chat room");
        }

        if (userOut) {
            comingbackToChatRoom();
        } else {
            m_inRoom = true;
            m_name = clientName;
            m_listener = l;
        }
    }

    @Override
    public void leave() throws ChatException {
//        System.out.println("Leave chat Room");
//        m_inRoom = false;
//        leavingChatRoom();
        System.out.println("Not implemented function, use quit button.");
    }

    @Override
    public void send(String msg) throws ChatException {
        if (!this.m_inRoom) {
            throw new ChatException("You are not in chat room");
        }
        //m_listener.deliver(msg);
        sendDataMessage(msg);
    }

    @Override
    public void rush() throws ChatException {
        //TODO
        rushBoolean = !rushBoolean;
        if (rushBoolean) {
            produceFakeMessages();
        }
    }

    /**
     * Creates a background thread whose only purpose is to fake received
     * messages
     */
    private void produceFakeMessages() {
        // This thread is to create fake messages...
        // You can comment
        Thread thread = new Thread(new Runnable() {
            public void run() {
                double base = System.currentTimeMillis();
                Random rand = new Random();
                while (rushBoolean) {
                    try {
                        int sleep = rand.nextInt(400);
                        Thread.sleep(800 + sleep);
                        int no = rand.nextInt(10);
                        double time = (double) System.currentTimeMillis();
                        time = (time - base) / 1000.0;
                        send("Fake" + no + " says: time is " + time);
                    } catch (Exception ex) {
                    }
                }
            }
        }, "Fake");
        thread.start();
    }

}
