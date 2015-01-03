/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 *
 * @author aquilest
 */
public class NioServerImpl extends nio.engine.NioServer {

    private int port = -1;
    private ServerSocketChannel serverSocket = null;
    private AcceptCallback acceptCallBack = null;

    public NioServerImpl(int port, AcceptCallback acceptCallback) throws IOException {
        this.serverSocket.open();
        this.serverSocket.configureBlocking(false);
        this.serverSocket.socket().bind(new InetSocketAddress(port));
        this.acceptCallBack = acceptCallback;
        this.port = port;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ServerSocketChannel getSSChannel() {
        return this.serverSocket;
    }

    public void setAcceptCallback(AcceptCallback callback) {
        this.acceptCallBack = callback;
    }

    public void fireAccept(NioChannelImpl channel) {
        this.acceptCallBack.accepted(this, channel);
    }

    public void fireClose(NioChannelImpl channel) {
        this.acceptCallBack.closed(channel);
    }

}
