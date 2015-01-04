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
    
    
    public RemoteUser(InetAddress ip) {
        super(ip);
    }
    
    public RemoteUser(NioChannelImpl nioChannel){
        super(nioChannel.getRemoteAddress().getAddress());
        this.nioChannel = nioChannel;
    }
    
    @Override
    public NioChannelImpl getNioChannel(){
        return this.nioChannel;
    }
        
}
