/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors;

import implementation.engine.ConnectCallback;
import implementation.engine.NioChannelImpl;
import java.net.InetAddress;

/**
 *
 * @author aquilest
 */
public class RemoteUser extends RemoteHost implements ConnectCallback {
    
    private NioChannelImpl nioChannel= null;
    private int index = -1;
    private ConnectCallback connectedCallback = null;
    private String name;
    
    
    public RemoteUser(InetAddress ip, int port, int index) {
        super(ip);
        setPort(port);
        this.index = index;
    }
    
    public RemoteUser(NioChannelImpl nioChannel, int index){
        super(nioChannel.getRemoteAddress().getAddress());
        this.nioChannel = nioChannel;
        this.index = index;
    }
    
    @Override
    public NioChannelImpl getNioChannel(){
        return this.nioChannel;
    }
    
    public void setNioChannel(NioChannelImpl channel){
        this.nioChannel = channel;
    }
    
    public int getIndex(){
        return this.index;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return this.name;
    }

    @Override
    public void closed(NioChannelImpl channel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connected(NioChannelImpl channel) {
        if(this.nioChannel == null){
            this.nioChannel = channel;
            this.connectedCallback.connected(channel);
        }
    }
    
    public void setConnectedCallback(ConnectCallback callback){
        this.connectedCallback = callback;
    }
       
 }
