/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.client;

import chat.descriptors.RemoteServer;
import chat.descriptors.RemoteUser;
import chat.descriptors.messages.SetListeningPortMsg;
import java.nio.ByteBuffer;
import implementation.engine.AcceptCallback;
import implementation.engine.ConnectCallback;
import implementation.engine.DeliverCallback;
import implementation.engine.NioChannelImpl;
import implementation.engine.NioEngineImpl;
import implementation.engine.NioServerImpl;
import java.net.InetAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;

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
    private LinkedList<RemoteUser> group; // Stock users

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
    
    public void initClient(String host, int port) throws ClientException{
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connected(NioChannelImpl channel) {
        if (this.remoteServer == null) {
            try {
                // Server not defined, that means this is the first connected
                channel.setDeliverCallback(this);
                this.remoteServer = new RemoteServer(channel);
                this.remoteServer.addMessageToQueue(new SetListeningPortMsg(
                        this.localServer.getPort()));
                this.nioEngine.registerNioChannel(remoteServer, SelectionKey.OP_WRITE);
                System.out.println("Server registed.");
            } catch (ClosedChannelException ex) {
                ex.printStackTrace();
                //TODO: Exception because channel can be registred;
            }
        } else {
            //TODO: This is connection with another that finish
        }
    }

    @Override
    public void deliver(NioChannelImpl channel, ByteBuffer bytes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
