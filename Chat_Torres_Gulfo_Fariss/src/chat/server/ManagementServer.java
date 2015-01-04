/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server;

import chat.descriptors.RemoteHost;
import chat.descriptors.RemoteUser;
import chat.descriptors.Utils;
import implementation.engine.NioEngineImpl;
import implementation.engine.NioServerImpl;
import implementation.engine.AcceptCallback;
import implementation.engine.ConnectCallback;
import implementation.engine.DeliverCallback;
import implementation.engine.NioChannelImpl;
import java.io.UnsupportedEncodingException;
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
        this(5000, 5959, 5);
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

    public void initServer() throws ServerException{
        this.nioLoop = new Thread(this.nioEngine, "NioLoopServer");
        this.nioLoop.start(); //TODO: Watch thread behavior
        startListening();
    }

    @Override
    public void accepted(NioServerImpl server, NioChannelImpl channel) {
        if (group.size() < this.maxUsers) { //If the group is not complete
            channel.setDeliverCallback(this); // Set callback of incomming message
            RemoteUser user = new RemoteUser(channel);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connected(NioChannelImpl channel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deliver(NioChannelImpl channel, ByteBuffer bytes) {
        byte[] msg = new byte[bytes.limit()];
        bytes.get(msg);
        RemoteHost user = (RemoteHost) whoHasThisNioChannel(channel);
        if (user.getPort() < 0) { // Listening port not setted 
            user.setPort(Utils.byteArrayToInt(msg));
            System.out.println("User " + user.toString() + ": Listening port setted.");
        } else { // print incomming message
            //TODO: Usefull to debug
            try {
                String str = new String(msg, "UTF-8");
                System.out.println("User " + user.toString() + " sends: \n" + str);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(ManagementServer.class.getName()).log(Level.SEVERE, null, ex);
            }
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

}
