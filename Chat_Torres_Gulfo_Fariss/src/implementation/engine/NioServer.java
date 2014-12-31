/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package implementation.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import nio.engine.AcceptCallback;

/**
 *
 * @author aquilest
 */
public class NioServer extends nio.engine.NioServer{
    private int port;
    private ServerSocketChannel serverSocket = null;
    private AcceptCallback acceptCallBack;
    
    public NioServer(int port, AcceptCallback acceptCallback) throws IOException{
        this.serverSocket.open();
        this.serverSocket.configureBlocking(false);
        this.serverSocket.socket().bind(new InetSocketAddress(port));       
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
    
    public ServerSocketChannel getSSChannel(){
        return this.serverSocket;
    }
    
    public void setAcceptCallback(AcceptCallback callback){
        this.acceptCallBack = callback;
    }
    
    public void fireAccept(NioChannel channel){
        this.acceptCallBack.accepted(this, channel);
    }
    
    public void fireClose(NioChannel channel){
        this.acceptCallBack.closed(channel);
    }
    
}
