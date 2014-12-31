/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation.engine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import nio.engine.AcceptCallback;
import nio.engine.ConnectCallback;

/**
 *
 * @author aquilest
 */
public class NioEngine extends nio.engine.NioEngine {

    private Selector selector;
    private java.util.List pendingChanges;

    public NioEngine() throws Exception {
        super();
        this.pendingChanges = new LinkedList();
        this.selector = SelectorProvider.provider().openSelector();
    }

    @Override
    public void mainloop() {
        while (true) { // Run until stop, processing available I/O operations
            try {
                //Process any pending change: change interested_OP or register a new socket.
                synchronized (this.pendingChanges) {
                    Iterator changes = this.pendingChanges.iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = (ChangeRequest) changes.next();
                        switch (change.type) {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.socket.keyFor(this.selector);
                                key.interestOps(change.ops);
                                break;
                            case ChangeRequest.REGISTER:
                                change.socket.register(this.selector, change.ops);
                                break;
                        }
                    }
                    this.pendingChanges.clear();
                }

                // Wait for some channel to be ready (or timeout)
                this.selector.select();

                //Get iterator on set of keys with I/O to process
                Iterator keyIter = this.selector.selectedKeys().iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = (SelectionKey) keyIter.next(); // Key is bit mask
                    keyIter.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    //When a Connection is ready to finish
                    if (key.isConnectable()) {
                        handleConnect(key);
                    }
                    // Server socket channel has pending connection requests?
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } // Client socket channel has pending data?
                    else if (key.isReadable()) {
                        handleRead(key);
                    } // Client socket channel is available for writing and
                    // key is valid (i.e., channel not closed)?
                    else if (key.isWritable()) {
                        handleWrite(key);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public NioServer listen(int port, AcceptCallback callback) throws IOException {
        NioServer nioServer = new NioServer(port, callback);
        SelectionKey key = nioServer.getSSChannel().register(selector, SelectionKey.OP_ACCEPT); //TODO: ChangeRequest if needed
        key.attach(nioServer);
        return nioServer;
    }

    @Override
    public void connect(InetAddress hostAddress, int port, ConnectCallback callback) throws UnknownHostException, SecurityException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void handleAccept(SelectionKey key) throws IOException {
        // If the channel is interested in OP_ACCEPT, then he has a NioServer attached
        NioServer nioServer = (NioServer) key.attachment();

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        NioChannel nioChannel = new NioChannel(socketChannel);

        nioServer.fireAccept(nioChannel);
    }
    
    public void handleConnect (SelectionKey key){
        //TODO
    }
    
    public void handleRead (SelectionKey key){
        //TODO
    }
    
    public void handleWrite (SelectionKey key){
        //TODO
    }

    public void registerNioChannel(NioChannel nioChannel, int op_int) throws ClosedChannelException {
        if (op_int != SelectionKey.OP_ACCEPT) {
            SelectionKey key = nioChannel.getChannel().register(selector, op_int);
            key.attach(nioChannel);
        }
    }

}
