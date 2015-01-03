/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server;

import implementation.engine.NioEngineImpl;
import implementation.engine.NioServerImpl;
import java.util.List;
import implementation.engine.AcceptCallback;
import implementation.engine.ConnectCallback;
import implementation.engine.DeliverCallback;
import implementation.engine.NioChannelImpl;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;

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
    private List group; // Stock users

    public ManagementServer(int minPort, int maxPort, int maxUsers) throws ServerException {
        try {
            this.nioEngine = new NioEngineImpl();
        } catch (Exception e) {
            throw new ServerException("Server can not be created");
        }
        this.minPort = minPort;
        this.maxPort = maxPort;
        this.maxUsers = maxUsers;
    }

    public ManagementServer() throws ServerException {
        this(5000, 5959, 5);
    }

    public void startServer() throws ServerException {
        for (int i = minPort; i <= maxPort; i++) {
            try {
                server = this.nioEngine.listen(i, this);
            } catch (Exception e) {
                System.err.println("Starting server in port " + i + " not possible.");
            }
        }
        if (server != null && server.getPort() > 0){
            System.out.println("Running server on port " + server.getPort());
        }else{
            throw new ServerException("Server can not start in ports " + minPort + " - "+
                    maxPort + ".");
        }
    }

    @Override
    public void accepted(NioServerImpl server, NioChannelImpl channel) {
        //TODO: Add Users
        try {
            this.nioEngine.registerNioChannel(channel, SelectionKey.OP_READ);
        } catch (ClosedChannelException ex) {
            //TODO;
            channel.close();
            System.err.println("New channel can not be registred. The connection has been closed.");
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
