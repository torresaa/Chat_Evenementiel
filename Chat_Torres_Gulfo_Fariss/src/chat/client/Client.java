/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.client;

import chat.descriptors.RemoteServer;
import chat.descriptors.RemoteUser;
import chat.descriptors.messages.AckServer;
import chat.descriptors.messages.ListeningPortMsg;
import chat.descriptors.messages.Message;
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
import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aquilest
 */
public class Client implements AcceptCallback, ConnectCallback, DeliverCallback {

    private NioEngineImpl nioEngine;
    private NioServerImpl localServer = null;
    private RemoteServer remoteServer = null;

    private int minPort;
    private int maxPort;
    private LinkedList<RemoteUser> group = null; // Stock users

    private String groupName = null;
    private int numberOfGroupMembers = -1;

    //Thread
    private Thread nioLoop;

    public Client(int minPort, int maxPort) throws ClientException {

        try {
            this.nioEngine = new NioEngineImpl();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ClientException("Client can not be created.");
        }

        this.minPort = minPort;
        this.maxPort = maxPort;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closed(NioChannelImpl channel) {
        //TODO: User reconnection
        //When tis method is called, the key for this channel has been 
        //erased. The only things to do is to lock fro the user in the lis of members
        //and then add it to the possible reconnection list
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
            //TODO: This is connection with another that finish
        }
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
                System.out.println("New group created, name: " + this.groupName
                        + " number of members: " + this.numberOfGroupMembers);
                break;
            case Message.GROUP_MEMBER_MSG:
                int indexValeu = bytes.getInt();
                byte[] ipBytes = new byte[4];
                InetAddress ip = null;
                 {
                    try {
                        ip = InetAddress.getByAddress(ipBytes);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                bytes.get(ipBytes);
                int listenigPort = bytes.getInt();
                RemoteUser user = new RemoteUser(ip, listenigPort, indexValeu);
                this.group.add(user);
                System.out.println("--new member: " + user.toString());

                if (group.size() == this.numberOfGroupMembers) {
                    this.remoteServer.addMessageToQueue(new AckServer());
                    nioEngine.changeOpInterest(channel, 
                            SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    initChat();
                }
                break;
        }

    }

    /**
     * init connnection with the member of the group that have bigger index than
     * this clien;
     */
    public void initChat() {
        System.out.println("Init Chat");
    }

}
