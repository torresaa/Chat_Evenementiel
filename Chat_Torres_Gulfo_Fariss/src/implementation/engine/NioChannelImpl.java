/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation.engine;

import chat.descriptors.Options;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aquilest
 */
public class NioChannelImpl extends nio.engine.NioChannel {

    private SocketChannel socketChannel;
    private DeliverCallback deliverCallback = null;
    private ConnectCallback connectCallback = null;

    public NioChannelImpl(SocketChannel channel) {
        this.socketChannel = channel;
    }

    @Override
    public SocketChannel getChannel() {
        return this.socketChannel;
    }

    @Override
    public void setDeliverCallback(DeliverCallback callback) {
        this.deliverCallback = callback;
    }

    public void setConnectCalllback(ConnectCallback callback) {
        this.connectCallback = callback;
    }

    public void fireDeliver() {
        ByteBuffer buffer = ByteBuffer.allocate(Options.INCOMING_BUFFER_SIZE);
        long bytesRead;

        try {
            bytesRead = socketChannel.read(buffer);
        } catch (IOException e) {
            System.err.println("Problem to read the message.");
            return;
        }

//        if (bytesRead == -1) {
//            // Did the other end close?
//            sc.close();
//            System.err.println("The remote connection has been closed.");
//            return;
//        }
        buffer.flip(); // Ready to read

        /**
         * Get bytes from buffer
         */
//        byte[] msg = new byte[buffer.limit()];
//        buffer.get(msg);
        this.deliverCallback.deliver(this, buffer);
    }

    public void fireConnect() {
        this.connectCallback.connected(this);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        try {
            InetSocketAddress remote = (InetSocketAddress)
                    this.socketChannel.getRemoteAddress();
            return remote;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void send(ByteBuffer buf) {
        while (buf.hasRemaining()) {
            try {
                this.socketChannel.write(buf);
            } catch (IOException ex) {
                //TODO: WriteNotPossible, Problem with the remote host
                ex.printStackTrace();
                Logger.getLogger(NioChannelImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void send(byte[] bytes, int offset, int length) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
