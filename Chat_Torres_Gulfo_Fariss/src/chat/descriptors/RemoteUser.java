/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.descriptors;

import implementation.engine.NioChannelImpl;
import java.net.InetAddress;

/**
 *
 * @author aquilest
 */
public class RemoteUser extends RemoteHost{
    
    private NioChannelImpl nioChannel;
    private int index = -1;
    
    
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
        
}
