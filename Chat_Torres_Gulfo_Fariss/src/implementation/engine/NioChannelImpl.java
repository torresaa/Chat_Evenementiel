/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation.engine;

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

    public void fireDeliver() throws SocketChannelCloseException {

        ByteBuffer msgLengthBuffer = ByteBuffer.allocate(4);

        int bytesRead = -1;
        while (msgLengthBuffer.position() < msgLengthBuffer.limit()) {
            try {
                bytesRead = socketChannel.read(msgLengthBuffer);
            } catch (IOException e) {
                System.err.println("Problem to read the message.");
                this.deliverCallback.closed(this);
                throw new SocketChannelCloseException();
            }
        }

        if (bytesRead < 0) {
            try {
                socketChannel.close();
            } catch (IOException ex) {
                Logger.getLogger(NioChannelImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.err.println("The remote connection has been closed.");
            this.deliverCallback.closed(this);
            throw new SocketChannelCloseException();
        }

        msgLengthBuffer.flip();

        int msgBufferLength = msgLengthBuffer.getInt();
        ByteBuffer msgBuffer = ByteBuffer.allocate(msgBufferLength);

        while (msgBuffer.position() < msgBuffer.limit()) {
            try {
                bytesRead = socketChannel.read(msgBuffer);
            } catch (IOException e) {
                System.err.println("Problem to read the message.");
                this.deliverCallback.closed(this);
                throw new SocketChannelCloseException();
            }
        }

        if (bytesRead < 0) {
            try {
                socketChannel.close();
            } catch (IOException ex) {
                Logger.getLogger(NioChannelImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.err.println("The remote connection has been closed.");
            this.deliverCallback.closed(this);
            throw new SocketChannelCloseException();
        }

        msgBuffer.flip(); // Ready to read

        this.deliverCallback.deliver(this, msgBuffer);
    }

    public void fireConnect() {
        this.connectCallback.connected(this);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        try {
            InetSocketAddress remote = (InetSocketAddress) this.socketChannel.getRemoteAddress();
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
        System.out.println("Close Channel");
        try {
            this.socketChannel.close();
        } catch (IOException ex) {
            Logger.getLogger(NioChannelImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
