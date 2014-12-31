/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation.engine;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import nio.engine.ConnectCallback;
import nio.engine.DeliverCallback;

/**
 *
 * @author aquilest
 */
public class NioChannel extends nio.engine.NioChannel{
    private SocketChannel socketChannel;
    private DeliverCallback deliverCallback = null;
    private ConnectCallback connectCallback = null;
    
    public NioChannel(SocketChannel channel){
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

    @Override
    public InetSocketAddress getRemoteAddress() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void send(ByteBuffer buf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
