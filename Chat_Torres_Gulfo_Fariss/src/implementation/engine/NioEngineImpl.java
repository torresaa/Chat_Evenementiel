/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation.engine;

import chat.descriptors.RemoteHost;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author aquilest
 */
public class NioEngineImpl extends nio.engine.NioEngine implements Runnable {

    private Selector selector;
    private java.util.List pendingChanges;

    public NioEngineImpl() throws Exception {
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
    public NioServerImpl listen(int port, AcceptCallback callback) throws IOException {
        NioServerImpl nioServer = new NioServerImpl(port, callback);
        SelectionKey key;
        key = nioServer.getSSChannel().register(this.selector.wakeup(), SelectionKey.OP_ACCEPT);
        key.attach(nioServer);
        return nioServer;
    }

    @Override
    public void connect(InetAddress hostAddress, int port, ConnectCallback callback) throws UnknownHostException, SecurityException, IOException {
        SocketChannel socket = SocketChannel.open();
        socket.configureBlocking(false);
        socket.connect(new InetSocketAddress(hostAddress, port));
        NioChannelImpl nioChannel = new NioChannelImpl(socket);
        nioChannel.setConnectCalllback(callback);
        SelectionKey key = socket.register(this.selector.wakeup(), SelectionKey.OP_CONNECT, nioChannel);
    }

    public void handleAccept(SelectionKey key) throws IOException {
        // If the channel is interested in OP_ACCEPT, then he has a NioServerImpl attached
        NioServerImpl nioServer = (NioServerImpl) key.attachment();

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        NioChannelImpl nioChannel = new NioChannelImpl(socketChannel);

        nioServer.fireAccept(nioChannel);
    }

    public void handleConnect(SelectionKey key) {
        /**
         * If this key is with OP_Connect, then it has a NioChannel with connect
         * call back seted
         */
        NioChannelImpl channel = (NioChannelImpl) key.attachment();
        channel.fireConnect();
    }

    public void handleRead(SelectionKey key) {
        RemoteHost host = (RemoteHost) key.attachment();
        host.getNioChannel().fireDeliver();
    }

    public void handleWrite(SelectionKey key) {
        RemoteHost host = (RemoteHost) key.attachment();
        byte[] msg = host.getMessageFromQueue();
        if (!host.queueIsEmpty()) { //There are message
            // Send first message of the Queue
            ByteBuffer buffer = ByteBuffer.allocate(msg.length);
            buffer.clear();
            buffer.put(msg);
            
            buffer.flip(); // Buffer ready to read
            
            host.getNioChannel().send(buffer);
        }

    }

    public void registerNioChannel(RemoteHost user, int op_int) throws ClosedChannelException {
        if (op_int != SelectionKey.OP_ACCEPT) {
            SelectionKey key = user.getNioChannel().getChannel().register(
                    selector.wakeup(), op_int, user);
        }
    }

    public void changeOpInterest(NioChannelImpl channel, int op_int) {
        //TODO
    }

    @Override
    public void run() {
        mainloop();
    }

}
